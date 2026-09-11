<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcPlanApi } from '#/api/mes/qcplan';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  disableQcPlan,
  deleteQcPlan,
  deleteQcPlanList,
  exportQcPlan,
  getQcPlanPage,
  publishQcPlan,
} from '#/api/mes/qcplan';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建检验方案 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑检验方案 */
function handleEdit(row: MesQcPlanApi.QcPlan) {
  formModalApi.setData(row).open();
}

/** 删除检验方案 */
async function handleDelete(row: MesQcPlanApi.QcPlan) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.code]),
  });
  try {
    await deleteQcPlan(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.code]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除检验方案 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteQcPlanList(checkedIds.value);
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
  records: MesQcPlanApi.QcPlan[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportQcPlan(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '检验方案.xls', source: data });
}

/** 发布检验方案 */
async function handlePublish(row: MesQcPlanApi.QcPlan) {
  const loadingInstance = ElLoading.service({
    text: `正在发布 ${row.code}`,
  });
  try {
    await publishQcPlan(row.id!);
    ElMessage.success('检验方案已发布');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 停用检验方案 */
async function handleDisable(row: MesQcPlanApi.QcPlan) {
  const loadingInstance = ElLoading.service({
    text: `正在停用 ${row.code}`,
  });
  try {
    await disableQcPlan(row.id!);
    ElMessage.success('检验方案已停用');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
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
          return await getQcPlanPage({
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
  } as VxeTableGridOptions<MesQcPlanApi.QcPlan>,
  gridEvents: {
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="检验方案列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['检验方案']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:qc-plan:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:qc-plan:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:qc-plan:delete'],
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
              auth: ['mes:qc-plan:update'],
              disabled: row.status !== 'DRAFT',
              onClick: handleEdit.bind(null, row),
            },
            {
              label: '发布',
              type: 'primary',
              link: true,
              auth: ['mes:qc-plan:publish'],
              disabled: row.status !== 'DRAFT',
              popConfirm: {
                title: `确认发布检验方案 ${row.code}？发布后不可编辑。`,
                confirm: handlePublish.bind(null, row),
              },
            },
            {
              label: '停用',
              type: 'danger',
              link: true,
              auth: ['mes:qc-plan:disable'],
              disabled: row.status !== 'PUBLISHED',
              popConfirm: {
                title: `确认停用检验方案 ${row.code}？`,
                confirm: handleDisable.bind(null, row),
              },
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:qc-plan:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.code]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
