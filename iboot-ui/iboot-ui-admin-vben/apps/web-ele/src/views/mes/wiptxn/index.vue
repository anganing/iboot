<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWipTxnApi } from '#/api/mes/wiptxn';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteWipTxn,
  deleteWipTxnList,
  exportWipTxn,
  getWipTxnPage,
} from '#/api/mes/wiptxn';
import WipTxnImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: WipTxnImport,
  destroyOnClose: true,
});

/** 导入MES-在制流转流水 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建MES-在制流转流水 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑MES-在制流转流水 */
function handleEdit(row: MesWipTxnApi.WipTxn) {
  formModalApi.setData(row).open();
}

/** 删除MES-在制流转流水 */
async function handleDelete(row: MesWipTxnApi.WipTxn) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.id]),
  });
  try {
    await deleteWipTxn(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.id]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除MES-在制流转流水 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteWipTxnList(checkedIds.value);
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
  records: MesWipTxnApi.WipTxn[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportWipTxn(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: 'MES-在制流转流水.xls', source: data });
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
          return await getWipTxnPage({
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
  } as VxeTableGridOptions<MesWipTxnApi.WipTxn>,
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
    <Grid table-title="MES-在制流转流水列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['MES-在制流转流水']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:wip-txn:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['mes:wip-txn:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:wip-txn:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['mes:wip-txn:delete'],
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
              auth: ['mes:wip-txn:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['mes:wip-txn:delete'],
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