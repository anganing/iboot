<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWorkOrderApi } from '#/api/mes/workorder';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportWorkOrder,
  getWorkOrderPage,
} from '#/api/mes/workorder';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 导出表格 */
async function handleExport() {
  const data = await exportWorkOrder(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '作业工单.xls', source: data });
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
          return await getWorkOrderPage({
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
  } as VxeTableGridOptions<MesWorkOrderApi.WorkOrder>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="作业工单列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:work-order:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
