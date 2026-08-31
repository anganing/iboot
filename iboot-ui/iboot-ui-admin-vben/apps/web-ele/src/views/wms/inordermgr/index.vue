<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteInOrder,
  deleteInOrderList,
  exportInOrder,
  getInOrderPage,
} from '#/api/wms/inordermgr';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import InOrderImport from './modules/import-form.vue';
import InOrderLineDrawer from './modules/in-order-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: InOrderLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: InOrderImport,
  destroyOnClose: true,
});

/** 导入入库单 */
function handleImport() {
  importFormModalApi.open();
}

/** 查看并维护当前单据的入库明细 */
function handleLines(row: WmsInOrderApi.InOrder) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建入库单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑入库单 */
function handleEdit(row: WmsInOrderApi.InOrder) {
  formModalApi.setData(row).open();
}

/** 删除入库单 */
async function handleDelete(row: WmsInOrderApi.InOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.inNo]),
  });
  try {
    await deleteInOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.inNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除入库单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteInOrderList(checkedIds.value);
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
  records: WmsInOrderApi.InOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportInOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '入库单.xls', source: data });
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
          return await getInOrderPage({
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
  } as VxeTableGridOptions<WmsInOrderApi.InOrder>,
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
    <Grid table-title="入库单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['入库单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:in-order:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:in-order:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:in-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:in-order:delete'],
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
              auth: ['wms:in-order:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:in-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:in-order:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.inNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
