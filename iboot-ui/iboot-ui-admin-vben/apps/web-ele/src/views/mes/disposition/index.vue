<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispositionApi } from '#/api/mes/disposition';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelDisposition,
  executeDisposition,
  deleteDisposition,
  deleteDispositionList,
  exportDisposition,
  getDispositionPage,
  submitDisposition,
} from '#/api/mes/disposition';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const router = useRouter();

function isApprovalRunning(row: MesDispositionApi.Disposition) {
  return row.approvalStatus === 1;
}

function handleProcessDetail(processInstanceId?: string) {
  if (processInstanceId) {
    router.push({ name: 'BpmProcessInstanceDetail', query: { id: processInstanceId } });
  }
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建不合格品处置单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑不合格品处置单 */
function handleEdit(row: MesDispositionApi.Disposition) {
  formModalApi.setData(row).open();
}

/** 删除不合格品处置单 */
async function handleDelete(row: MesDispositionApi.Disposition) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.dispositionNo]),
  });
  try {
    await deleteDisposition(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.dispositionNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 提交处置单 */
async function handleSubmit(row: MesDispositionApi.Disposition) {
  await submitDisposition(row.id!);
  ElMessage.success('处置单已提交');
  handleRefresh();
}

/** 执行处置单 */
async function handleExecute(row: MesDispositionApi.Disposition) {
  const loadingInstance = ElLoading.service({
    text: `正在执行 ${row.dispositionNo}`,
  });
  try {
    await executeDisposition(row.id!);
    ElMessage.success('处置已执行');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消处置单 */
async function handleCancel(row: MesDispositionApi.Disposition) {
  await cancelDisposition(row.id!);
  ElMessage.success('处置单已取消');
  handleRefresh();
}

/** 批量删除不合格品处置单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteDispositionList(checkedIds.value);
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
  records: MesDispositionApi.Disposition[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportDisposition(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '不合格品处置单.xls', source: data });
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
          return await getDispositionPage({
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
  } as VxeTableGridOptions<MesDispositionApi.Disposition>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="不合格品处置单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['不合格品处置单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:disposition:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:disposition:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:disposition:delete'],
              disabled: isEmpty(checkedIds),
              onClick: handleDeleteBatch,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['mes:disposition:update'],
              disabled: row.status !== 'DRAFT' || isApprovalRunning(row),
              onClick: handleEdit.bind(null, row),
            },
            {
              label: ['CONCESSION', 'SCRAP'].includes(row.dispositionType || '') ? '提交审批' : '提交',
              type: 'primary',
              link: true,
              auth: ['mes:disposition:submit'],
              disabled: row.status !== 'DRAFT',
              popConfirm: {
                title: `确认提交处置单 ${row.dispositionNo}？`,
                confirm: handleSubmit.bind(null, row),
              },
            },
            {
              label: '执行',
              type: 'primary',
              link: true,
              auth: ['mes:disposition:execute'],
              disabled: row.status !== 'APPROVED',
              popConfirm: {
                title: `确认执行处置单 ${row.dispositionNo}？执行会联动库存或返工。`,
                confirm: handleExecute.bind(null, row),
              },
            },
            {
              label: '取消',
              type: 'danger',
              link: true,
              auth: ['mes:disposition:cancel'],
              disabled: !['DRAFT', 'SUBMITTED'].includes(row.status || '') || isApprovalRunning(row),
              popConfirm: {
                title: `确认取消处置单 ${row.dispositionNo}？`,
                confirm: handleCancel.bind(null, row),
              },
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:disposition:delete'],
              disabled: row.status !== 'DRAFT' || isApprovalRunning(row),
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.dispositionNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
            {
              label: '审批进度',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              ifShow: !!row.processInstanceId,
              onClick: handleProcessDetail.bind(null, row.processInstanceId),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
