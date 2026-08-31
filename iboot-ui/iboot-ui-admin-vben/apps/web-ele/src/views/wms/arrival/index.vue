<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteArrival,
  deleteArrivalList,
  exportArrival,
  getArrivalPage,
} from '#/api/wms/arrival';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import ArrivalLineDrawer from './modules/arrival-line-drawer.vue';
import Form from './modules/form.vue';
import ArrivalImport from './modules/import-form.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: ArrivalLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: ArrivalImport,
  destroyOnClose: true,
});

/** 导入到货登记 */
function handleImport() {
  importFormModalApi.open();
}

/** 查看并维护当前单据的到货明细 */
function handleLines(row: WmsArrivalApi.Arrival) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建到货登记 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑到货登记 */
function handleEdit(row: WmsArrivalApi.Arrival) {
  formModalApi.setData(row).open();
}

/** 删除到货登记 */
async function handleDelete(row: WmsArrivalApi.Arrival) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.arrivalNo]),
  });
  try {
    await deleteArrival(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.arrivalNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除到货登记 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteArrivalList(checkedIds.value);
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
  records: WmsArrivalApi.Arrival[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportArrival(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '到货登记.xls', source: data });
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
          return await getArrivalPage({
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
  } as VxeTableGridOptions<WmsArrivalApi.Arrival>,
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
    <Grid table-title="到货登记列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['到货登记']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:arrival:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:arrival:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:arrival:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:arrival:delete'],
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
              auth: ['wms:arrival:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:arrival:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:arrival:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.arrivalNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
