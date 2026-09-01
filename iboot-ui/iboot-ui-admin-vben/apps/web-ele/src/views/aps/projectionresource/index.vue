<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionResourceApi } from '#/api/aps/projectionresource';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportProjectionResource,
  getProjectionResourcePage,
} from '#/api/aps/projectionresource';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 导出表格 */
async function handleExport() {
  const data = await exportProjectionResource(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '资源投影.xls', source: data });
}

const [Grid, gridApi] = useVbenVxeGrid({
  formOptions: { schema: useGridFormSchema() },
  gridOptions: {
    columns: useGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => getProjectionResourcePage({
          pageNo: page.currentPage,
          pageSize: page.pageSize,
          ...formValues,
        }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true },
  } as VxeTableGridOptions<ApsProjectionResourceApi.ProjectionResource>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="资源投影列表">
      <template #toolbar-tools>
        <TableAction :actions="[
          {
            label: $t('ui.actionTitle.export'), type: 'primary', icon: ACTION_ICON.DOWNLOAD,
            auth: ['aps:projection-resource:export'], onClick: handleExport,
          },
        ]" />
      </template>
    </Grid>
  </Page>
</template>
