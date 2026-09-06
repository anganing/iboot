<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdReportApi } from '#/api/mes/prodreport';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelProdReport,
  exportProdReport,
  getProdReportPage,
} from '#/api/mes/prodreport';
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

/** 创建生产报工 */
function handleCreate() {
  formModalApi.setData(null).open();
}

function canCancelReport(row: MesProdReportApi.ProdReport) {
  return row.status === 'SUBMITTED';
}

/** 取消报工：反向扣减三级数量并写反向流水 */
async function handleCancel(row: MesProdReportApi.ProdReport) {
  let reason: string;
  try {
    const { value } = await ElMessageBox.prompt(
      `确认取消报工【${row.reportNo}】？会反向扣减三级数量并写反向流水。`,
      '取消生产报工',
      {
        confirmButtonText: '确认取消',
        cancelButtonText: '返回',
        inputPlaceholder: '请输入取消原因',
        inputValidator: (input: string) =>
          input && input.trim().length > 0 ? true : '取消原因不能为空',
        type: 'warning',
      },
    );
    reason = value;
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在取消...' });
  try {
    await cancelProdReport({ id: row.id!, reason });
    ElMessage.success('已取消');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportProdReport(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '生产报工记录.xls', source: data });
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
          return await getProdReportPage({
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
  } as VxeTableGridOptions<MesProdReportApi.ProdReport>,
});
</script>

<template>
  <Page auto-content-height>
    <FormModal @success="handleRefresh" />
    <Grid table-title="生产报工记录列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.create', ['生产报工']),
              type: 'primary',
              icon: ACTION_ICON.ADD,
              auth: ['mes:prod-report:create'],
              onClick: handleCreate,
            },
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:prod-report:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '取消',
              type: 'warning',
              link: true,
              icon: 'lucide:ban',
              auth: ['mes:prod-report:cancel'],
              disabled: !canCancelReport(row),
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
