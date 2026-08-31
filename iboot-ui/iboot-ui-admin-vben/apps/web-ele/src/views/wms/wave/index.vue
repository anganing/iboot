<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWaveApi } from '#/api/wms/wave';

import { ref } from 'vue';

import { confirm, Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart, isEmpty } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  deleteWave,
  deleteWaveList,
  exportWave,
  getWavePage,
} from '#/api/wms/wave';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import WaveImport from './modules/import-form.vue';
import WaveLineDrawer from './modules/wave-line-drawer.vue';

const [LineDrawer, lineDrawerApi] = useVbenDrawer({
  connectedComponent: WaveLineDrawer,
  destroyOnClose: true,
});
const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
const [ImportFormModal, importFormModalApi] = useVbenModal({
  connectedComponent: WaveImport,
  destroyOnClose: true,
});

/** 导入出库波次 */
function handleImport() {
  importFormModalApi.open();
}

/** 查看并维护当前单据的波次明细 */
function handleLines(row: WmsWaveApi.Wave) {
  lineDrawerApi.setData(row).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 创建出库波次 */
function handleCreate() {
  formModalApi.setData(null).open();
}

/** 编辑出库波次 */
function handleEdit(row: WmsWaveApi.Wave) {
  formModalApi.setData(row).open();
}

/** 删除出库波次 */
async function handleDelete(row: WmsWaveApi.Wave) {
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deleting', [row.waveNo]),
  });
  try {
    await deleteWave(row.id!);
    ElMessage.success($t('ui.actionMessage.deleteSuccess', [row.waveNo]));
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 批量删除出库波次 */
async function handleDeleteBatch() {
  await confirm($t('ui.actionMessage.deleteBatchConfirm'));
  const loadingInstance = ElLoading.service({
    text: $t('ui.actionMessage.deletingBatch'),
  });
  try {
    await deleteWaveList(checkedIds.value);
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
  records: WmsWaveApi.Wave[];
}) {
  checkedIds.value = records.map((item) => item.id!);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportWave(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '出库波次.xls', source: data });
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
          return await getWavePage({
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
  } as VxeTableGridOptions<WmsWaveApi.Wave>,
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
    <Grid table-title="出库波次列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['出库波次']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['wms:wave:create'],
              onClick: handleCreate,
            },
            {
              label: '导入',
              type: 'primary',
              auth: ['wms:wave:import'],
              onClick: handleImport,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:wave:export'],
              onClick: handleExport,
            },
            {
              label: $t('ui.actionTitle.deleteBatch'),
              type: 'danger',
              icon: ACTION_ICON.DELETE,
              auth: ['wms:wave:delete'],
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
              auth: ['wms:wave:query'],
              onClick: handleLines.bind(null, row),
            },
            {
              label: $t('common.edit'),
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:wave:update'],
              onClick: handleEdit.bind(null, row),
            },
            {
              label: $t('common.delete'),
              type: 'danger',
              link: true,
              icon: ACTION_ICON.DELETE,
              auth: ['wms:wave:delete'],
              popConfirm: {
                title: $t('ui.actionMessage.deleteConfirm', [row.waveNo]),
                confirm: handleDelete.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
