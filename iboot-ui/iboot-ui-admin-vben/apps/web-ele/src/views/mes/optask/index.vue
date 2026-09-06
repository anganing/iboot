<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOpTask,
  getOpTaskPage,
} from '#/api/mes/optask';
import { $t } from '#/locales';

import { useGridColumns, useGridFormSchema } from './data';
import ActionForm from './modules/action-form.vue';
import SkipForm from './modules/skip-form.vue';

const [ActionModal, actionModalApi] = useVbenModal({
  connectedComponent: ActionForm,
  destroyOnClose: true,
});
const [SkipModal, skipModalApi] = useVbenModal({
  connectedComponent: SkipForm,
  destroyOnClose: true,
});

function canStart(row: MesOpTaskApi.OpTask) {
  return row.status === 'READY';
}

function canPause(row: MesOpTaskApi.OpTask) {
  return row.status === 'RUNNING';
}

function canResume(row: MesOpTaskApi.OpTask) {
  return row.status === 'PAUSED' || row.status === 'HOLD';
}

function canFinish(row: MesOpTaskApi.OpTask) {
  return row.status === 'RUNNING';
}

function canSkip(row: MesOpTaskApi.OpTask) {
  return row.status === 'PENDING' || row.status === 'READY';
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

function handleAction(
  action: 'start' | 'pause' | 'resume' | 'finish',
  row: MesOpTaskApi.OpTask,
) {
  actionModalApi.setData({ action, row }).open();
}

function handleSkip(row: MesOpTaskApi.OpTask) {
  skipModalApi.setData(row).open();
}

async function handleFinish(row: MesOpTaskApi.OpTask) {
  handleAction('finish', row);
}

async function handleResume(row: MesOpTaskApi.OpTask) {
  handleAction('resume', row);
}

async function handleStart(row: MesOpTaskApi.OpTask) {
  handleAction('start', row);
}

/** 导出表格 */
async function handleExport() {
  const data = await exportOpTask(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '工序任务.xls', source: data });
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
          return await getOpTaskPage({
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
  } as VxeTableGridOptions<MesOpTaskApi.OpTask>,
});
</script>

<template>
  <Page auto-content-height>
    <ActionModal @success="handleRefresh" />
    <SkipModal @success="handleRefresh" />
    <Grid table-title="工序任务列表">
      <template #toolbar-tools>
        <TableAction
          :actions="[
            {
              label: $t('ui.actionTitle.export'),
              type: 'primary',
              icon: ACTION_ICON.DOWNLOAD,
              auth: ['mes:op-task:export'],
              onClick: handleExport,
            },
          ]"
        />
      </template>
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '开工',
              type: 'primary',
              link: true,
              icon: 'lucide:play',
              auth: ['mes:op-task:start'],
              disabled: !canStart(row),
              onClick: handleStart.bind(null, row),
            },
            {
              label: '暂停',
              type: 'warning',
              link: true,
              icon: 'lucide:pause',
              auth: ['mes:op-task:pause'],
              disabled: !canPause(row),
              onClick: handleAction.bind(null, 'pause', row),
            },
            {
              label: '恢复',
              type: 'primary',
              link: true,
              icon: 'lucide:rotate-cw',
              auth: ['mes:op-task:resume'],
              disabled: !canResume(row),
              onClick: handleResume.bind(null, row),
            },
            {
              label: '完工',
              type: 'primary',
              link: true,
              icon: 'lucide:check-check',
              auth: ['mes:op-task:finish'],
              disabled: !canFinish(row),
              onClick: handleFinish.bind(null, row),
            },
            {
              label: '跳序',
              type: 'warning',
              link: true,
              icon: 'lucide:skip-forward',
              auth: ['mes:op-task:skip'],
              disabled: !canSkip(row),
              onClick: handleSkip.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
