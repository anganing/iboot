<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  assignQcTask,
  cancelQcTask,
  exportQcTask,
  getQcTaskPage,
  startInspectQcTask,
  submitQcTaskConclusion,
} from '#/api/mes/qctask';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { useGridColumns, useGridFormSchema } from './data';
import Detail from './modules/qc-task-detail.vue';

const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: Detail,
  destroyOnClose: true,
});

/** 双击质检任务号（链接列）打开只读详情；本页无写型"明细"按钮，按规范 3.5 双击是唯一入口 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesQcTaskApi.QcTask;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 指派检验员 */
async function handleAssign(row: MesQcTaskApi.QcTask) {
  const loadingInstance = ElLoading.service({
    text: `正在指派 ${row.taskNo}`,
  });
  try {
    // 检验任务页先给当前登录主管常用动作；作业页再支持精确选人
    await assignQcTask(row.id!, row.assignedInspectorId || 1);
    ElMessage.success('检验员已指派');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 开始检验 */
async function handleStart(row: MesQcTaskApi.QcTask) {
  const loadingInstance = ElLoading.service({
    text: `正在开始 ${row.taskNo}`,
  });
  try {
    await startInspectQcTask(row.id!);
    ElMessage.success('检验已开始');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 提交检验结论 */
async function handleSubmit(row: MesQcTaskApi.QcTask) {
  const loadingInstance = ElLoading.service({
    text: `正在提交 ${row.taskNo}`,
  });
  try {
    await submitQcTaskConclusion({
      id: row.id!,
      conclusion: row.conclusion || 'QUALIFIED',
      qtyQualified: row.qtyQualified || 0,
      qtyRejected: row.qtyRejected || 0,
    });
    ElMessage.success('结论已提交');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消质检任务 */
async function handleCancel(row: MesQcTaskApi.QcTask) {
  await cancelQcTask(row.id!, '来源业务取消');
  ElMessage.success('质检任务已取消');
  handleRefresh();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportQcTask(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '质检任务.xls', source: data });
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
          return await getQcTaskPage({
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
  } as VxeTableGridOptions<MesQcTaskApi.QcTask>,
});
</script>

<template>
  <Page auto-content-height>
    <DetailDrawer />
    <Grid table-title="质检任务列表" @cell-dblclick="handleCellDblclick">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:qc-task:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '指派',
              type: 'primary',
              link: true,
              auth: ['mes:qc-task:assign'],
              disabled: row.status !== 'CREATED',
              tooltip:
                row.status === 'CREATED'
                  ? undefined
                  : '仅已创建的任务可指派检验员',
              onClick: handleAssign.bind(null, row),
            },
            {
              label: '开始检验',
              type: 'primary',
              link: true,
              auth: ['mes:qc-task:start-inspect'],
              disabled: row.status !== 'ASSIGNED',
              tooltip:
                row.status === 'ASSIGNED'
                  ? undefined
                  : '任务已指派后才能开始检验',
              onClick: handleStart.bind(null, row),
            },
            {
              label: '提交结论',
              type: 'primary',
              link: true,
              auth: ['mes:qc-task:submit-conclusion'],
              disabled: row.status !== 'RESULT_POSTING',
              tooltip:
                row.status === 'RESULT_POSTING'
                  ? undefined
                  : '检验结果录入完成后才能提交结论',
              onClick: handleSubmit.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: '取消',
              type: 'danger',
              link: true,
              auth: ['mes:qc-task:cancel'],
              disabled: !['CREATED', 'ASSIGNED'].includes(row.status || ''),
              popConfirm: {
                title: `确认取消质检任务 ${row.taskNo}？`,
                confirm: handleCancel.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
