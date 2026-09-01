<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsLoadApi } from '#/api/aps/load';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportLoad, getLoadPage, refreshLoad } from '#/api/aps/load';

import { useGridColumns, useGridFormSchema } from './data';

async function handleRefreshLoad(row: ApsLoadApi.Load) {
  if (!row.versionId) {
    ElMessage.warning('当前记录缺少排程版本，无法重算');
    return;
  }
  const loading = ElLoading.service({ text: '正在重算资源负荷...' });
  try {
    await refreshLoad(row.versionId);
    ElMessage.success('资源负荷已重算');
    await gridApi.query();
  } finally {
    loading.close();
  }
}

async function handleExport() {
  const data = await exportLoad(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '资源负荷.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getLoadPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsLoadApi.Load>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="资源负荷列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '导出',
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:load:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '重算负荷',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.REFRESH,
              auth: ['aps:load:update'],
              onClick: handleRefreshLoad.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
