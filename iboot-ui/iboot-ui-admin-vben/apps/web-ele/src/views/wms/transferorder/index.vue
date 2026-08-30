<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { ref } from 'vue';

import { confirm, Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage, ElTabs, ElTabPane } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteTransferOrder,
  deleteTransferOrderList,
  exportTransferOrder,
  getTransferOrderPage,
} from '#/api/wms/transferorder';
import TransferOrderImport from './modules/import-form.vue';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import TransferOrderLineList from './modules/transfer-order-line-list.vue';

 /** 子表的列表 */
const subTabsName = ref('transferOrderLine')
const selectTransferOrder = ref<WmsTransferOrderApi.TransferOrder>();
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: TransferOrderImport,
  destroyOnClose: true,
});

/** 导入移库调拨单 */
function handleImport() {
  importFormModalApi.open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建移库调拨单 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑移库调拨单 */
function handleEdit(row: WmsTransferOrderApi.TransferOrder) {
  formModalApi.setData(row).open();
}

/** 删除移库调拨单 */
async function handleDelete(row: WmsTransferOrderApi.TransferOrder) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.transferNo]),
  });
  try {
    await deleteTransferOrder(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.transferNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除移库调拨单 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteTransferOrderList(checkedIds.value);
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
  records: WmsTransferOrderApi.TransferOrder[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportTransferOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '移库调拨单.xls', source: data });
}


const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    height: '600px',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getTransferOrderPage({
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
  } as VxeTableGridOptions<WmsTransferOrderApi.TransferOrder>,
  gridEvents: {
    cellClick: ({ row }: { row: WmsTransferOrderApi.TransferOrder}) => {
      selectTransferOrder.value = row;
    },
    checkboxAll: handleRowCheckboxChange,
    checkboxChange: handleRowCheckboxChange,
  },
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <ImportFormModal @success="handleRefresh" />
   <div>
    <Grid table-title="移库调拨单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['移库调拨单']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:transfer-order:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:transfer-order:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:transfer-order:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
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
              auth: ['wms:transfer-order:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:transfer-order:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.transferNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
     <!-- 子表的表单 -->
    <ElTabs v-model="subTabsName" class="mt-2">
          <ElTabPane name="transferOrderLine" label="调拨明细">
            <TransferOrderLineList :order-id="selectTransferOrder?.id" />
          </ElTabPane>
    </ElTabs>
    </div>
  </Page>
</template>