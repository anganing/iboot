<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesKitSnapshotApi } from '#/api/mes/kitsnapshot';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteKitSnapshot,
  deleteKitSnapshotList,
  exportKitSnapshot,
  getKitSnapshotPage,
} from '#/api/mes/kitsnapshot';
import KitSnapshotImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: KitSnapshotImport,
  destroyOnClose: true,
});

/** 导入MES-齐套检查快照 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建MES-齐套检查快照 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑MES-齐套检查快照 */
function handleEdit(row: MesKitSnapshotApi.KitSnapshot) {
  formModalApi.setData(row).open();
}

/** 删除MES-齐套检查快照 */
async function handleDelete(row: MesKitSnapshotApi.KitSnapshot) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteKitSnapshot(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除MES-齐套检查快照 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteKitSnapshotList(checkedIds.value);
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
  records: MesKitSnapshotApi.KitSnapshot[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportKitSnapshot(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'MES-齐套检查快照.xls', source: data });
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
          return await getKitSnapshotPage({
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
  } as VxeTableGridOptions<MesKitSnapshotApi.KitSnapshot>,
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
    <Grid table-title="MES-齐套检查快照列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['MES-齐套检查快照']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:kit-snapshot:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['mes:kit-snapshot:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:kit-snapshot:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:kit-snapshot:delete'],
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
              auth: ['mes:kit-snapshot:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:kit-snapshot:delete'],
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