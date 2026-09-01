<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionMatApi } from '#/api/aps/projectionmat';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportProjectionMat, getProjectionMatPage } from '#/api/aps/projectionmat';

import { useGridColumns, useGridFormSchema } from './data';

async function handleExport() {
  const data = await exportProjectionMat(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '物料投影.xls', source: data });
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
          getProjectionMatPage({ pageNo: page.currentPage, pageSize: page.pageSize, ...formValues }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsProjectionMatApi.ProjectionMat>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="物料投影列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            { label: '导出', type: 'primary', icon: ACTION_ICON.DOWNLOAD, auth: ['aps:projection-mat:export'], onClick: handleExport },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
