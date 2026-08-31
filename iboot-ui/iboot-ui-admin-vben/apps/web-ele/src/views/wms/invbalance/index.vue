<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvBalanceApi } from '#/api/wms/invbalance';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportInvBalance, getInvBalancePage } from '#/api/wms/invbalance';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';


/** 导出表格 */
async function handleExport() {
  const data = await exportInvBalance(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '库存余额.xls', source: data });
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
          return await getInvBalancePage({
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
  } as VxeTableGridOptions<WmsInvBalanceApi.InvBalance>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="库存余额列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:inv-balance:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
