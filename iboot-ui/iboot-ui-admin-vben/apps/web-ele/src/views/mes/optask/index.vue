<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { Page, useVbenDrawer, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import { ElLoading, ElMessage, ElMessageBox } from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOpTask,
  getOpTaskPage,
  pauseOpTask,
  resumeOpTask,
  startOpTask,
} from '#/api/mes/optask';
import { $t } from '#/locales';
import { isCodeLinkColumn } from '#/utils';

import { OP_TASK_STATUS, useGridColumns, useGridFormSchema } from './data';
import FinishForm from './modules/finish-form.vue';
import OptaskDetail from './modules/optask-detail.vue';
import SkipForm from './modules/skip-form.vue';

const [FinishModal, finishModalApi] = useVbenModal({
  connectedComponent: FinishForm,
  destroyOnClose: true,
});
const [SkipModal, skipModalApi] = useVbenModal({
  connectedComponent: SkipForm,
  destroyOnClose: true,
});
const [DetailDrawer, detailDrawerApi] = useVbenDrawer({
  connectedComponent: OptaskDetail,
  destroyOnClose: true,
});

/**
 * 以下门控逐条对齐后端 MesOpTaskStateMachine 的迁移表，不写成前端自由判断：
 * 判断宽了会出现“按钮可点但后端抛状态非法”，判断窄了会把合法动作藏起来。
 * 完工只放开 RUNNING 是因为 QC_WAITING 由检验合格联动到 DONE，不由人工完工。
 */
function canStart(row: MesOpTaskApi.OpTask) {
  return row.status === OP_TASK_STATUS.READY;
}

function canPause(row: MesOpTaskApi.OpTask) {
  return row.status === OP_TASK_STATUS.RUNNING;
}

function canResume(row: MesOpTaskApi.OpTask) {
  const status = row.status ?? '';
  return status === OP_TASK_STATUS.PAUSED || status === OP_TASK_STATUS.HOLD;
}

function canFinish(row: MesOpTaskApi.OpTask) {
  return row.status === OP_TASK_STATUS.RUNNING;
}

function canSkip(row: MesOpTaskApi.OpTask) {
  const status = row.status ?? '';
  return status === OP_TASK_STATUS.PENDING || status === OP_TASK_STATUS.READY;
}

/** 刷新表格 */
function handleRefresh() {
  gridApi.query();
}

/** 开工：经开工门禁校验技能资质，门禁拦截原因由全局拦截器完整弹出 */
async function handleStart(row: MesOpTaskApi.OpTask) {
  try {
    await ElMessageBox.confirm(
      `确认开工工序【${row.taskNo}】？将校验作业人员技能资质与开工门禁。`,
      '工序开工',
      {
        confirmButtonText: '开工',
        cancelButtonText: '取消',
        type: 'warning',
      },
    );
  } catch {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在开工...' });
  try {
    await startOpTask({ id: row.id! });
    ElMessage.success('已开工');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 暂停：原因写进在制流水 */
async function handlePause(row: MesOpTaskApi.OpTask) {
  const reason = await promptReason(
    `确认暂停工序【${row.taskNo}】？请填写暂停原因。`,
    '工序暂停',
  );
  if (reason === undefined) {
    return;
  }
  const loadingInstance = ElLoading.service({ text: '正在暂停...' });
  try {
    await pauseOpTask({ id: row.id!, reason });
    ElMessage.success('已暂停');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 恢复：暂停与质量挂起都回到加工中 */
async function handleResume(row: MesOpTaskApi.OpTask) {
  const loadingInstance = ElLoading.service({ text: '正在恢复...' });
  try {
    await resumeOpTask({ id: row.id! });
    ElMessage.success('已恢复');
    handleRefresh();
  } finally {
    loadingInstance.close();
  }
}

/** 完工：数量对账声明，两个都留空按后端累计值校验 */
function handleFinish(row: MesOpTaskApi.OpTask) {
  finishModalApi.setData(row).open();
}

/** 跳序：绕过工艺，按任务快照的跳序策略放行 */
function handleSkip(row: MesOpTaskApi.OpTask) {
  skipModalApi.setData(row).open();
}

async function promptReason(message: string, title: string) {
  try {
    const { value } = await ElMessageBox.prompt(message, title, {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputPlaceholder: '请输入原因',
      inputValidator: (input: string) =>
        input && input.trim().length > 0 ? true : '原因不能为空',
      type: 'warning',
    });
    return value;
  } catch {
    return undefined;
  }
}

/** 导出表格 */
async function handleExport() {
  const data = await exportOpTask(await gridApi.formApi.getValues());
  downloadFileFromBlobPart({ fileName: '工序任务.xls', source: data });
}

/**
 * 双击工序任务号（链接列）打开只读详情。
 *
 * 只认链接列，整行与其余单元格一律不响应：工序任务列表 19 列、行高密集，
 * 做成整行热区会频繁误开抽屉；工序编码列也不能当入口（同工单多行重复）。
 */
function handleCellDblclick({
  column,
  row,
}: {
  column: { params?: unknown };
  row: MesOpTaskApi.OpTask;
}) {
  if (!isCodeLinkColumn(column)) return;
  detailDrawerApi.setData({ id: row.id }).open();
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
    <FinishModal @success="handleRefresh" />
    <SkipModal @success="handleRefresh" />
    <DetailDrawer />
    <Grid table-title="工序任务列表" @cell-dblclick="handleCellDblclick">
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
              auth: ['mes:op-task:start'],
              disabled: !canStart(row),
              tooltip: canStart(row)
                ? undefined
                : '仅「待开工」的工序可开工；待排产需先下达计划，已开工或已完工不可重复开工',
              onClick: handleStart.bind(null, row),
            },
            {
              label: '完工',
              type: 'primary',
              link: true,
              auth: ['mes:op-task:finish'],
              disabled: !canFinish(row),
              tooltip: canFinish(row)
                ? undefined
                : '仅「加工中」的工序可完工；需检验的工序由报工与检验联动推进',
              onClick: handleFinish.bind(null, row),
            },
            {
              label: '跳序',
              type: 'primary',
              link: true,
              auth: ['mes:op-task:skip'],
              disabled: !canSkip(row),
              tooltip: canSkip(row)
                ? undefined
                : '跳序只允许发生在未开工（待排产/待开工）的工序上，已开工后只能走返工或报废',
              onClick: handleSkip.bind(null, row),
            },
          ]"
          :drop-down-actions="[
            {
              // type 一律 primary：TableAction 的「更多」触发按钮取的是
              // getDropdownList[0].type，这里写 warning 会把「更多」一起染色
              label: '暂停',
              type: 'primary',
              link: true,
              auth: ['mes:op-task:pause'],
              disabled: !canPause(row),
              tooltip: canPause(row) ? undefined : '仅「加工中」的工序可暂停',
              onClick: handlePause.bind(null, row),
            },
            {
              label: '恢复',
              type: 'primary',
              link: true,
              auth: ['mes:op-task:resume'],
              disabled: !canResume(row),
              tooltip: canResume(row)
                ? undefined
                : '只有已暂停或质量挂起的工序可以恢复加工',
              onClick: handleResume.bind(null, row),
            },
          ]"
        />
      </template>
    </Grid>
  </Page>
</template>
