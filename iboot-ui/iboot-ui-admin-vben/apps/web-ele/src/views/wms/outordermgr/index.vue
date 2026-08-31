<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteOutOrder,
  deleteOutOrderList,
  exportOutOrder,
  getOutOrderPage,
} from '#/api/wms/outordermgr';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import OutOrderImport from './modules/import-form.vue';
import OutOrderLineDrawer from './modules/out-order-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: OutOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: OutOrderImport,
  destroyOnClose: true,
});

/** 导入出库单 */
function handleImport() {
  importFormModalApi.open();
}

/** 查看并维护当前单据的出库明细 */
function handleLines(row: WmsOutOrderApi.OutOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建出库单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑出库单 */
function handleEdit(row: WmsOutOrderApi.OutOrder) {
  formModalApi.setData(row).open();
}

/** 删除出库单 */
async function handleDelete(row: WmsOutOrderApi.OutOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.outNo]),
  });
  try {
    await deleteOutOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.outNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除出库单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteOutOrderList(checkedIds.value);
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
  records: WmsOutOrderApi.OutOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportOutOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '出库单.xls', source: data });
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
          return await getOutOrderPage({
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
      isCurrent: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<WmsOutOrderApi.OutOrder>,
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
    <LineDrawer />
    <Grid table-title="出库单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['出库单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:out-order:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:out-order:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:out-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
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
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['wms:out-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:out-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:out-order:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.outNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
