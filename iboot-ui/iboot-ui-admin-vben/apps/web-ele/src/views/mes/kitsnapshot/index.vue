<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesKitSnapshotApi } from '#/api/mes/kitsnapshot';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportKitSnapshot, getKitSnapshotPage } from '#/api/mes/kitsnapshot';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 导出表格 */
async function handleExport() {
  const data = await exportKitSnapshot(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '齐套检查快照.xls', source: data });
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
          return await getKitSnapshotPage({
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
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MesKitSnapshotApi.KitSnapshot>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="齐套检查快照列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:kit-snapshot:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
