<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportSolverSnapshot, getSolverSnapshotPage } from '#/api/aps/solversnapshot';

import { useGridColumns, useGridFormSchema } from './data';

async function handleExport() {
  const data = await exportSolverSnapshot(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '求解快照.xls', source: data });
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
          getSolverSnapshotPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsSolverSnapshotApi.SolverSnapshot>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="求解快照列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: '导出',
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['aps:solver-snapshot:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
