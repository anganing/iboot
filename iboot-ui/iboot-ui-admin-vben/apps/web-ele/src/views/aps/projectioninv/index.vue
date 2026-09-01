<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionInvApi } from '#/api/aps/projectioninv';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportProjectionInv, getProjectionInvPage } from '#/api/aps/projectioninv';

import { useGridColumns, useGridFormSchema } from './data';

async function handleExport() {
  const data = await exportProjectionInv(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '库存投影.xls', source: data });
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
          getProjectionInvPage({ pageNo: page.currentPage, pageSize: page.pageSize, ...formValues }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsProjectionInvApi.ProjectionInv>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="库存投影列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            { label: '导出', type: 'primary', icon: ACTION_ICON.DOWNLOAD, auth: ['aps:projection-inv:export'], onClick: handleExport },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
