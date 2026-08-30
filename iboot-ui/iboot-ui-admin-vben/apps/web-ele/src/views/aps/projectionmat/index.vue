<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionMatApi } from '#/api/aps/projectionmat';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteProjectionMat,
  deleteProjectionMatList,
  exportProjectionMat,
  getProjectionMatPage,
} from '#/api/aps/projectionmat';
import ProjectionMatImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: ProjectionMatImport,
  destroyOnClose: true,
});

/** 导入APS-MDM物料投影 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建APS-MDM物料投影 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑APS-MDM物料投影 */
function handleEdit(row: ApsProjectionMatApi.ProjectionMat) {
  formModalApi.setData(row).open();
}

/** 删除APS-MDM物料投影 */
async function handleDelete(row: ApsProjectionMatApi.ProjectionMat) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteProjectionMat(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除APS-MDM物料投影 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteProjectionMatList(checkedIds.value);
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
  records: ApsProjectionMatApi.ProjectionMat[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportProjectionMat(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'APS-MDM物料投影.xls', source: data });
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
          return await getProjectionMatPage({
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
  } as VxeTableGridOptions<ApsProjectionMatApi.ProjectionMat>,
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
    <Grid table-title="APS-MDM物料投影列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['APS-MDM物料投影']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['aps:projection-mat:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['aps:projection-mat:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:projection-mat:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['aps:projection-mat:delete'],
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
              auth: ['aps:projection-mat:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['aps:projection-mat:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.id]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>