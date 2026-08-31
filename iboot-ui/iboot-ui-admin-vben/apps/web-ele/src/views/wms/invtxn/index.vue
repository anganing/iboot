<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvTxnApi } from '#/api/wms/invtxn';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportInvTxn, getInvTxnPage } from '#/api/wms/invtxn';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';


/** 导出表格 */
async function handleExport() {
  const data = await exportInvTxn(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '库存流水.xls', source: data });
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
          return await getInvTxnPage({
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
  } as VxeTableGridOptions<WmsInvTxnApi.InvTxn>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="库存流水列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:inv-txn:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
