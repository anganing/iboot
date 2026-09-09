<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { Page, useVbenModal } from '@vben/common-ui';
import { downloadFileFromBlobPart } from '@vben/utils';

import {
  ElButton,
  ElDropdown,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import {
  exportOpTask,
  getOpTaskPage,
  pauseOpTask,
  resumeOpTask,
  startOpTask,
} from '#/api/mes/optask';
import { $t } from '#/locales';

import { OP_TASK_STATUS, useGridColumns, useGridFormSchema } from './data';
import FinishForm from './modules/finish-form.vue';
import SkipForm from './modules/skip-form.vue';

const [FinishModal, finishModalApi] = useVbenModal({
  connectedComponent: FinishForm,
  destroyOnClose: true,
});
const [SkipModal, skipModalApi] = useVbenModal({
  connectedComponent: SkipForm,
  destroyOnClose: true,
});

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
        <div class="flex items-center justify-center gap-1">
          <ElButton
            v-access:code="['mes:op-task:start']"
            :disabled="!canStart(row)"
            link
            type="primary"
            @click="handleStart(row)"
            >
开工
</ElButton>
          <ElButton
            v-access:code="['mes:op-task:finish']"
            :disabled="!canFinish(row)"
            link
            type="primary"
            @click="handleFinish(row)"
            >
完工
</ElButton>
          <ElDropdown placement="bottom-end" trigger="click">
            <ElButton link type="primary">更多</ElButton>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-access:code="['mes:op-task:pause']"
                  :disabled="!canPause(row)"
                  @click="handlePause(row)"
                  >
暂停
</el-dropdown-item>
                <el-dropdown-item
                  v-access:code="['mes:op-task:resume']"
                  :disabled="!canResume(row)"
                  @click="handleResume(row)"
                  >
恢复
</el-dropdown-item>
                <el-dropdown-item
                  v-access:code="['mes:op-task:skip']"
                  :disabled="!canSkip(row)"
                  @click="handleSkip(row)"
                  >
跳序
</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </ElDropdown>
        </div>
      </template>
    </Grid>
  </Page>
</template>
