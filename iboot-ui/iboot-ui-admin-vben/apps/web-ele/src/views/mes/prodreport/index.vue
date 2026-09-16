<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdReportApi } from '#/api/mes/prodreport';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelProdReport,
  exportProdReport,
  getProdReportPage,
} from '#/api/mes/prodreport';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { MES_PROD_REPORT_STATUS } from '../order-form';
import { useGridColumns, useGridFormSchema } from './data';
import Form from './modules/form.vue';
import ProdReportDetail from './modules/prod-report-detail.vue';

const [FormModal, formModalApi] = useVbenModal({
  connectedComponent: Form,
  destroyOnClose: true,
});
/** 报工记录没有修改入口，双击报工单号是看全字段（幂等键、起止时间）的唯一途径 */
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: ProdReportDetail,
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
  return row.status === MES_PROD_REPORT_STATUS.SUBMITTED;
}

/** 打开只读详情 */
function handleDetail(row: MesProdReportApi.ProdReport) {
  detailDrawerApi.setData({ id: row.id }).open();
}

/** 双击报工单号（链接列）打开只读详情；单击行不开，避免误触 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesProdReportApi.ProdReport;
}) {
  if (!isCodeLinkColumn(column)) return;
  handleDetail(row);
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
    <DetailDrawer />
    <Grid
      table-title="生产报工记录列表"
      @cell-dblclick="handleCellDblclick"
    >
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
              tooltip: canCancelReport(row)
                ? undefined
                : '报工已取消，不能重复取消；取消会反向扣减三级数量并写反向流水',
              onClick: handleCancel.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
