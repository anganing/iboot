<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdOrderApi } from '#/api/mes/prodorder';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import {
  ElButton,
  ElDropdown,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelProdOrder,
  checkKitProdOrder,
  closeProdOrder,
  deleteProdOrder,
  deleteProdOrderList,
  exportProdOrder,
  getProdOrderPage,
  holdProdOrder,
  releaseProdOrder,
  resumeProdOrder,
} from '#/api/mes/prodorder';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ProdOrderImport from './modules/import-form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: ProdOrderImport,
  destroyOnClose: true,
});

function canEdit(row: MesProdOrderApi.ProdOrder) {
  return row.status === 'DRAFT';
}

function canRelease(row: MesProdOrderApi.ProdOrder) {
  return row.status === 'DRAFT';
}

function canHold(row: MesProdOrderApi.ProdOrder) {
  return row.status === 'RUNNING';
}

function canResume(row: MesProdOrderApi.ProdOrder) {
  return row.status === 'HOLD';
}

function canClose(row: MesProdOrderApi.ProdOrder) {
  return row.status === 'DONE';
}

function canCancelOrder(row: MesProdOrderApi.ProdOrder) {
  return ['DRAFT', 'RELEASED', 'READY'].includes(row.status ?? '');
}

function canCheckKit(row: MesProdOrderApi.ProdOrder) {
  return ['RELEASED', 'READY', 'RUNNING'].includes(row.status ?? '');
}

/** 导入生产工单 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建生产工单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑生产工单 */
function handleEdit(row: MesProdOrderApi.ProdOrder) {
  formModalApi.setData(row).open();
}

/** 删除生产工单 */
async function handleDelete(row: MesProdOrderApi.ProdOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.orderNo]),
  });
  try {
    await deleteProdOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.orderNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除生产工单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteProdOrderList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 下达生产工单：锁 BOM/路线版本并展开作业工单与工序任务 */
async function handleRelease(row: MesProdOrderApi.ProdOrder) {
  try {
    await ElMessageBox.confirm(
      `确认下达生产工单【${row.orderNo}】？下达后会锁定 BOM 与工艺路线版本，并展开作业工单与工序任务。`,
      '下达生产工单',
      {
        confirmButtonText: '下达',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在下达...' });
  try {
    await releaseProdOrder({ id: row.id! });
    ElMessage.success('下达成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 挂起生产工单 */
async function handleHold(row: MesProdOrderApi.ProdOrder) {
  const reason = await promptReason(
    `确认挂起生产工单【${row.orderNo}】？`,
    '挂起生产工单',
  );
  if (reason === undefined) {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在挂起...' });
  try {
    await holdProdOrder({ id: row.id!, reason });
    ElMessage.success('已挂起');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 恢复生产工单 */
async function handleResume(row: MesProdOrderApi.ProdOrder) {
  const loadingInstance = ElLoading.service({ text: '正在恢复...' });
  try {
    await resumeProdOrder({ id: row.id! });
    ElMessage.success('已恢复');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 关闭生产工单：释放预留并反冲倒冲件 */
async function handleClose(row: MesProdOrderApi.ProdOrder) {
  try {
    await ElMessageBox.confirm(
      `确认关闭生产工单【${row.orderNo}】？关闭会释放剩余预留，并按实际产量反冲倒冲件扣料。`,
      '关闭生产工单',
      {
        confirmButtonText: '关闭',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在关闭...' });
  try {
    const result = await closeProdOrder({ id: row.id! });
    const unreturnedCount = result.unreturnedLines?.length ?? 0;
    ElMessage.success(
      unreturnedCount > 0
        ? `关闭成功，${unreturnedCount} 行多余领料待退回`
        : '关闭成功',
    );
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消生产工单：仅开工前三态允许 */
async function handleCancel(row: MesProdOrderApi.ProdOrder) {
  const reason = await promptReason(
    `确认取消生产工单【${row.orderNo}】？会级联取消作业工单与工序任务。`,
    '取消生产工单',
  );
  if (reason === undefined) {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在取消...' });
  try {
    await cancelProdOrder({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 齐套检查：整单、作业工单、工序任务三档按行内点击默认走整单 */
async function handleCheckKit(row: MesProdOrderApi.ProdOrder) {
  const loadingInstance = ElLoading.service({ text: '正在齐套检查...' });
  try {
    const result = await checkKitProdOrder({
      id: row.id!,
      checkScope: 'PROD_ORDER',
    });
    ElMessage.success(`齐套检查完成：${result.result ?? ''}`);
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

async function promptReason(message: string, title: string) {
  try {
    const { value } = await ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因',
      inputValidator: (input: string) =>
        input && input.trim().length > 0 ? true : '原因不能为空',
      type: 'warning',
    });
    return value;
  } catch {
    return undefined;
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesProdOrderApi.ProdOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportProdOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '生产工单.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getProdOrderPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesProdOrderApi.ProdOrder>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <ImportFormModal @success="handleRefresh" />
    <Grid table-title="生产工单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['生产工单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:prod-order:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['mes:prod-order:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:prod-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:prod-order:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <div class="flex items-center justify-center gap-1">
          <ElButton
            v-access:code="['mes:prod-order:release']"
            :disabled="!canRelease(row)"
            link
            type="primary"
            @click="handleRelease(row)"
            >下达</ElButton
          >
          <ElButton
            v-access:code="['mes:prod-order:check-kit']"
            :disabled="!canCheckKit(row)"
            link
            type="primary"
            @click="handleCheckKit(row)"
            >齐套检查</ElButton
          >
          <ElButton
            v-access:code="['mes:prod-order:hold']"
            :disabled="!canHold(row)"
            link
            type="warning"
            @click="handleHold(row)"
            >挂起</ElButton
          >
          <ElDropdown placement="bottom-end" trigger="click">
            <ElButton link type="primary">更多</ElButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-access:code="['mes:prod-order:resume']"
                  :disabled="!canResume(row)"
                  @click="handleResume(row)"
                  >恢复</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:prod-order:close']"
                  :disabled="!canClose(row)"
                  @click="handleClose(row)"
                  >关闭</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:prod-order:cancel']"
                  :disabled="!canCancelOrder(row)"
                  @click="handleCancel(row)"
                  >取消</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:prod-order:update']"
                  :disabled="!canEdit(row)"
                  @click="handleEdit(row)"
                  >修改</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:prod-order:delete']"
                  :disabled="!canEdit(row)"
                  @click="handleDelete(row)"
                  >删除</el-dropdown-item
                >
              </el-dropdown-menu>
            </template>
          </ElDropdown>
        </div>
      </template>
    </Grid>
  </Page>
</template>
