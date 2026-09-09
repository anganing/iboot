<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdDemandApi } from '#/api/mes/proddemand';

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
  cancelProdDemand,
  confirmProdDemand,
  deleteProdDemand,
  deleteProdDemandList,
  exportProdDemand,
  getProdDemandPage,
  prodDemandToProdOrder,
} from '#/api/mes/proddemand';
import { $t } from '#/locales';

import { MES_DEMAND_STATUS } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ProdDemandImport from './modules/import-form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: ProdDemandImport,
  destroyOnClose: true,
});

function canConfirm(row: MesProdDemandApi.ProdDemand) {
  return row.status === MES_DEMAND_STATUS.DRAFT;
}

function canCancel(row: MesProdDemandApi.ProdDemand) {
  return row.status !== MES_DEMAND_STATUS.DONE && row.status !== MES_DEMAND_STATUS.CANCELLED;
}

function canToProdOrder(row: MesProdDemandApi.ProdDemand) {
  return (
    row.status === MES_DEMAND_STATUS.CONFIRMED ||
    row.status === MES_DEMAND_STATUS.PLANNED
  );
}

/** 导入生产需求 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建生产需求 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑生产需求 */
function handleEdit(row: MesProdDemandApi.ProdDemand) {
  formModalApi.setData(row).open();
}

/** 删除生产需求 */
async function handleDelete(row: MesProdDemandApi.ProdDemand) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.demandNo]),
  });
  try {
    await deleteProdDemand(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.demandNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除生产需求 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteProdDemandList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 确认生产需求：新建态才允许，后续由排产与转单接手 */
async function handleConfirm(row: MesProdDemandApi.ProdDemand) {
  try {
    await ElMessageBox.confirm(
      `确认生产需求【${row.demandNo}】？确认后需求进入已确认状态，不能再修改主体字段。`,
      '确认生产需求',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在确认...' });
  try {
    await confirmProdDemand({ id: row.id! });
    ElMessage.success('确认成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消生产需求：非终态都可取消，取消原因必填 */
async function handleCancel(row: MesProdDemandApi.ProdDemand) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消生产需求【${row.demandNo}】？请填写取消原因。`,
      '取消生产需求',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '请输入取消原因',
        inputValidator: (input: string) =>
          input && input.trim().length > 0 ? true : '取消原因不能为空',
        type: 'warning',
      },
    );
    reason = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在取消...' });
  try {
    await cancelProdDemand({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 生产需求转生产工单：支持分批转单，转满才置已下达 */
async function handleToProdOrder(row: MesProdDemandApi.ProdDemand) {
  let qtyText: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `将生产需求【${row.demandNo}】转为生产工单。请输入本次转单数量。`,
      '生产需求转工单',
      {
        confirmButtonText: '转单',
        cancelButtonText: '取消',
        inputPlaceholder: `最多可转 ${row.qty ?? 0}`,
        inputType: 'number',
        inputValidator: (input: string) => {
          const qty = Number(input);
          return Number.isFinite(qty) && qty > 0 ? true : '转单数量必须大于 0';
        },
      },
    );
    qtyText = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在转单...' });
  try {
    await prodDemandToProdOrder({ id: row.id!, qty: Number(qtyText) });
    ElMessage.success('转单成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesProdDemandApi.ProdDemand[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportProdDemand(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '生产需求.xls', source: data });
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
          return await getProdDemandPage({
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
  } as VxeTableGridOptions<MesProdDemandApi.ProdDemand>,
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
    <Grid table-title="生产需求列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['生产需求']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:prod-demand:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['mes:prod-demand:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:prod-demand:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:prod-demand:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <div class="flex items-center justify-center gap-1">
          <ElButton
            v-access:code="['mes:prod-demand:confirm']"
            :disabled="!canConfirm(row)"
            link
            type="primary"
            @click="handleConfirm(row)"
          >
            确认
          </ElButton>
          <ElButton
            v-access:code="['mes:prod-demand:to-prod-order']"
            :disabled="!canToProdOrder(row)"
            link
            type="primary"
            @click="handleToProdOrder(row)"
          >
            转工单
          </ElButton>
          <ElDropdown placement="bottom-end" trigger="click">
            <ElButton link type="primary">更多</ElButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-access:code="['mes:prod-demand:update']"
                  :disabled="!canConfirm(row)"
                  @click="handleEdit(row)"
                >
                  修改
                </el-dropdown-item>
                <el-dropdown-item
                  v-access:code="['mes:prod-demand:cancel']"
                  :disabled="!canCancel(row)"
                  @click="handleCancel(row)"
                >
                  取消
                </el-dropdown-item>
                <el-dropdown-item
                  v-access:code="['mes:prod-demand:delete']"
                  :disabled="!canConfirm(row)"
                  @click="handleDelete(row)"
                >
                  删除
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </ElDropdown>
        </div>
      </template>
    </Grid>
  </Page>
</template>
