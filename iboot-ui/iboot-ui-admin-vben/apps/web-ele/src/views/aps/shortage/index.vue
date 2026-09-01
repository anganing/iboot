<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsShortageApi } from '#/api/aps/shortage';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';
import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportShortage,
  getShortagePage,
  ignoreShortage,
  refreshShortage,
} from '#/api/aps/shortage';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import HandleForm from './modules/handle-form.vue';

const [HandleFormModal, handleFormModalApi] = useVbenModal({
  connectedComponent: HandleForm,
  destroyOnClose: true,
});

function handleRefresh() {
  gridApi.query();
}

function handleHandle(row: ApsShortageApi.Shortage) {
  handleFormModalApi.setData(row).open();
}

async function handleIgnore(row: ApsShortageApi.Shortage) {
  const loading = ElLoading.service({ text: '正在忽略缺料...' });
  try {
    await ignoreShortage(row.id!);
    ElMessage.success('缺料已忽略');
    handleRefresh();
  } finally {
    loading.close();
  }
}

async function handleRefreshShortage(row: ApsShortageApi.Shortage) {
  const loading = ElLoading.service({ text: '正在按库存投影重算缺料...' });
  try {
    await refreshShortage(row.versionId!);
    ElMessage.success('缺料清单已刷新');
    handleRefresh();
  } finally {
    loading.close();
  }
}

async function handleExport() {
  const data = await exportShortage(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '排程缺料清单.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => getShortagePage({
          pageNo: page.currentPage,
          pageSize: page.pageSize,
          ...formValues,
        }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsShortageApi.Shortage>,
});
</script>

<template>
  <Page auto-content-height>
    <HandleFormModal @success="handleRefresh" />
    <Grid table-title="排程缺料清单">
      <template #toolbar-tools>
        <TableAction :actions="[
          {
            label: $t('ui.actionTitle.export'), type: 'primary', icon: ACTION_ICON.DOWNLOAD,
            auth: ['aps:shortage:export'], onClick: handleExport,
          },
        ]" />
      </template>
      <template #actions="{ row }">
        <TableAction :actions="[
          {
            label: '刷新', type: 'primary', link: true, icon: ACTION_ICON.REFRESH,
            auth: ['aps:shortage:update'],
            popConfirm: {
              title: `按最新库存投影重算版本 ${row.versionNo} 的缺料清单吗？`,
              confirm: handleRefreshShortage.bind(null, row),
            },
          },
          {
            label: '处理', type: 'primary', link: true, icon: ACTION_ICON.EDIT,
            auth: ['aps:shortage:update'], onClick: handleHandle.bind(null, row),
          },
          {
            label: '忽略', type: 'warning', link: true,
            auth: ['aps:shortage:update'], disabled: row.status === 'IGNORED',
            popConfirm: {
              title: `忽略物料【${row.matCode}】的短缺提示吗？`,
              confirm: handleIgnore.bind(null, row),
            },
          },
        ]" />
      </template>
    </Grid>
  </Page>
</template>
