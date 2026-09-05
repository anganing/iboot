<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsBatchApi } from '#/api/wms/batchmgr';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { exportBatch, getBatchPage } from '#/api/wms/batchmgr';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/**
 * 维护批属性
 *
 * 批次由入库动作注册，界面上只能补录"注册之后才知道"的六个批属性（炉批号、生产日期、失效日期、
 * 状态、备注）。物料、批次号、序列号、供应商是批次的身份，余额与流水按 batchId 记账，不开放修改。
 */
function handleUpdateAttr(row: WmsBatchApi.Batch) {
  formModalApi.setData(row).open();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportBatch(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '批次.xls', source: data });
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
          return await getBatchPage({
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
  } as VxeTableGridOptions<WmsBatchApi.Batch>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="批次列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['wms:batch:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '维护批属性',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.EDIT,
              auth: ['wms:batch:update-attr'],
              onClick: handleUpdateAttr.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
