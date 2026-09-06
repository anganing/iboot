<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
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
  cancelMaterialReq,
  confirmMaterialReq,
  deleteMaterialReq,
  deleteMaterialReqList,
  exportMaterialReq,
  getMaterialReqPage,
  submitMaterialReq,
} from '#/api/mes/materialreq';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import MaterialReqLineDrawer from './modules/material-req-line-drawer.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
/** 明细走独立抽屉，列表页不内嵌平铺子表 */
const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: MaterialReqLineDrawer,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建领退补料申请 */
function handleCreate() {
  formModalApi.setData(null).open();
}

function isDraft(row: MesMaterialReqApi.MaterialReq) {
  return row.status === 'DRAFT';
}

function canSubmit(row: MesMaterialReqApi.MaterialReq) {
  return row.status === 'DRAFT';
}

function canConfirm(row: MesMaterialReqApi.MaterialReq) {
  return row.status === 'SUBMITTED';
}

function canCancel(row: MesMaterialReqApi.MaterialReq) {
  return row.status === 'DRAFT' || row.status === 'SUBMITTED';
}

/** 提交申请：领料与补料逐行占库存预留 */
async function handleSubmit(row: MesMaterialReqApi.MaterialReq) {
  try {
    await ElMessageBox.confirm(
      `确认提交领退补料申请【${row.reqNo}】？领料与补料会逐行占库存预留。`,
      '提交申请',
      {
        confirmButtonText: '确认提交',
        cancelButtonText: '返回',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在提交...' });
  try {
    await submitMaterialReq({ id: row.id! });
    ElMessage.success('提交成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 确认申请：一次性过账，剩余量另开补料单 */
async function handleConfirm(row: MesMaterialReqApi.MaterialReq) {
  try {
    await ElMessageBox.confirm(
      `确认领退补料申请【${row.reqNo}】？将全部行按剩余量发料/退料并过账。`,
      '确认发料',
      {
        confirmButtonText: '确认过账',
        cancelButtonText: '返回',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在确认过账...' });
  try {
    await confirmMaterialReq({ id: row.id! });
    ElMessage.success('确认成功');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消申请：草稿直接取消，已提交先释放预留 */
async function handleCancel(row: MesMaterialReqApi.MaterialReq) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消领退补料申请【${row.reqNo}】？请填写取消原因。`,
      '取消申请',
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
    await cancelMaterialReq({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 编辑领退补料申请 */
function handleEdit(row: MesMaterialReqApi.MaterialReq) {
  formModalApi.setData(row).open();
}

/** 打开申请明细抽屉 */
function handleLines(row: MesMaterialReqApi.MaterialReq) {
  lineDrawerApi.setData(row).open();
}

/** 删除领退补料申请 */
async function handleDelete(row: MesMaterialReqApi.MaterialReq) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.reqNo]),
  });
  try {
    await deleteMaterialReq(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.reqNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除领退补料申请 */
async function handleDeleteBatch() {
  await confirm('确认删除所选领退补料申请？申请明细会一并级联删除。');
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteMaterialReqList(checkedIds.value);
    checkedIds.value = [];
    ElMessage.success($t('ui.actionMessage.deleteSuccess'));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

const checkedIds = ref<number[]>([]);
function handleRowCheckboxChange({
  records,
}: {
  records: MesMaterialReqApi.MaterialReq[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportMaterialReq(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '领退补料申请.xls', source: data });
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
          return await getMaterialReqPage({
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
  } as VxeTableGridOptions<MesMaterialReqApi.MaterialReq>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <LineDrawer />
    <Grid table-title="领退补料申请列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['领退补料申请']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:material-req:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:material-req:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:material-req:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <div class="flex items-center justify-center gap-1">
          <TableAction
            :actions="[
              {
                label: '明细',
                type: 'primary',
                link: true,
                icon: ACTION_ICON.VIEW,
                auth: ['mes:material-req:query'],
                onClick: handleLines.bind(null, row),
              },
            ]"
          />
          <ElButton
            v-access:code="['mes:material-req:submit']"
            :disabled="!canSubmit(row)"
            link
            type="primary"
            @click="handleSubmit(row)"
            >提交</ElButton
          >
          <ElButton
            v-access:code="['mes:material-req:confirm']"
            :disabled="!canConfirm(row)"
            link
            type="primary"
            @click="handleConfirm(row)"
            >确认</ElButton
          >
          <ElDropdown placement="bottom-end" trigger="click">
            <ElButton link type="primary">更多</ElButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-access:code="['mes:material-req:cancel']"
                  :disabled="!canCancel(row)"
                  @click="handleCancel(row)"
                  >取消</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:material-req:update']"
                  :disabled="!isDraft(row)"
                  @click="handleEdit(row)"
                  >修改</el-dropdown-item
                >
                <el-dropdown-item
                  v-access:code="['mes:material-req:delete']"
                  :disabled="!isDraft(row)"
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
