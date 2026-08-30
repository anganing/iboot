<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionResourceApi } from '#/api/aps/projectionresource';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteProjectionResource,
  deleteProjectionResourceList,
  exportProjectionResource,
  getProjectionResourcePage,
} from '#/api/aps/projectionresource';
import ProjectionResourceImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: ProjectionResourceImport,
  destroyOnClose: true,
});

/** 导入APS-MDM资源与日历投影 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建APS-MDM资源与日历投影 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑APS-MDM资源与日历投影 */
function handleEdit(row: ApsProjectionResourceApi.ProjectionResource) {
  formModalApi.setData(row).open();
}

/** 删除APS-MDM资源与日历投影 */
async function handleDelete(row: ApsProjectionResourceApi.ProjectionResource) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteProjectionResource(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除APS-MDM资源与日历投影 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteProjectionResourceList(checkedIds.value);
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
  records: ApsProjectionResourceApi.ProjectionResource[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportProjectionResource(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'APS-MDM资源与日历投影.xls', source: data });
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
          return await getProjectionResourcePage({
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
  } as VxeTableGridOptions<ApsProjectionResourceApi.ProjectionResource>,
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
    <Grid table-title="APS-MDM资源与日历投影列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['APS-MDM资源与日历投影']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['aps:projection-resource:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['aps:projection-resource:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:projection-resource:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['aps:projection-resource:delete'],
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
              auth: ['aps:projection-resource:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['aps:projection-resource:delete'],
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