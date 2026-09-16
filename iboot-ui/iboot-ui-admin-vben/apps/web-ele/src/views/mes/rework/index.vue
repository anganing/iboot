<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesReworkApi } from '#/api/mes/rework';

import { Page } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  cancelRework,
  exportRework,
  finishRework,
  getReworkPage,
  releaseRework,
} from '#/api/mes/rework';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 终态：已完工与已取消都没有出边，取消动作在这两个状态下只能置灰说明原因 */
function isReworkFinal(row: MesReworkApi.Rework) {
  return row.status === 'DONE' || row.status === 'CANCELLED';
}

/** 下达返工任务 */
async function handleRelease(row: MesReworkApi.Rework) {
  const loadingInstance = ElLoading.service({
    text: `正在下达 ${row.reworkNo}`,
  });
  try {
    await releaseRework(row.id!);
    ElMessage.success('返工任务已下达');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 返工完工 */
async function handleFinish(row: MesReworkApi.Rework) {
  const loadingInstance = ElLoading.service({
    text: `正在完工 ${row.reworkNo}`,
  });
  try {
    await finishRework(row.id!);
    ElMessage.success('返工已完工');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 取消返工任务 */
async function handleCancel(row: MesReworkApi.Rework) {
  await cancelRework(row.id!);
  ElMessage.success('返工任务已取消');
  handleRefresh();
}

/** 导出表格 */
async function handleExport() {
  const data = await exportRework(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '返工任务.xls', source: data });
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
          return await getReworkPage({
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
  } as VxeTableGridOptions<MesReworkApi.Rework>,
});
</script>

<template>
  <Page auto-content-height>
    <Grid table-title="返工任务列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:rework:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '下达',
              type: 'primary',
              link: true,
              auth: ['mes:rework:release'],
              disabled: row.status !== 'DRAFT',
              tooltip:
                row.status === 'DRAFT' ? undefined : '仅草稿状态可下达返工任务',
              onClick: handleRelease.bind(null, row),
            },
            {
              label: '完工',
              type: 'primary',
              link: true,
              auth: ['mes:rework:finish'],
              disabled: row.status !== 'RUNNING',
              tooltip:
                row.status === 'RUNNING'
                  ? undefined
                  : '下达后才进入返工中，返工中才能报完工',
              onClick: handleFinish.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              label: '取消',
              type: 'danger',
              link: true,
              auth: ['mes:rework:cancel'],
              disabled: isReworkFinal(row),
              popConfirm: {
                title: `确认取消返工任务 ${row.reworkNo}？`,
                confirm: handleCancel.bind(null, row),
              },
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
