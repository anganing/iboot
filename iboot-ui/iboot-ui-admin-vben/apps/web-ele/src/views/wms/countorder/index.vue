<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteCountOrder,
  deleteCountOrderList,
  exportCountOrder,
  getCountOrderPage,
} from '#/api/wms/countorder';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import CountOrderLineDrawer from './modules/count-order-line-drawer.vue';
import Form from './modules/form.vue';
import CountOrderImport from './modules/import-form.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: CountOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: CountOrderImport,
  destroyOnClose: true,
});

/** 导入盘点单 */
function handleImport() {
  importFormModalApi.open();
}

/** 查看并维护当前单据的盘点明细 */
function handleLines(row: WmsCountOrderApi.CountOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建盘点单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑盘点单 */
function handleEdit(row: WmsCountOrderApi.CountOrder) {
  formModalApi.setData(row).open();
}

/** 删除盘点单 */
async function handleDelete(row: WmsCountOrderApi.CountOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.countNo]),
  });
  try {
    await deleteCountOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.countNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除盘点单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteCountOrderList(checkedIds.value);
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
  records: WmsCountOrderApi.CountOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportCountOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '盘点单.xls', source: data });
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
          return await getCountOrderPage({
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
  } as VxeTableGridOptions<WmsCountOrderApi.CountOrder>,
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
    <Grid table-title="盘点单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['盘点单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:count-order:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:count-order:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:count-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:count-order:delete'],
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
              auth: ['wms:count-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:count-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:count-order:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.countNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
