<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWipTxnApi } from '#/api/mes/wiptxn';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportWipTxn, getWipTxnPage } from '#/api/mes/wiptxn';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { useGridColumns, useGridFormSchema } from './data';
import WipTxnDetail from './modules/wip-txn-detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: WipTxnDetail,
  destroyOnClose: true,
});

/** 双击事务编号（链接列）打开只读详情；账表没有操作列，这是唯一的详情入口 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesWipTxnApi.WipTxn;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportWipTxn(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '在制流转流水.xls', source: data });
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
          return await getWipTxnPage({
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
  } as VxeTableGridOptions<MesWipTxnApi.WipTxn>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid
      table-title="在制流转流水列表"
      @cell-dblclick="handleCellDblclick"
    >
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:wip-txn:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
