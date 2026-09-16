<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { computed, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';

import {
  ElAlert,
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
  ElMessageBox,
} from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { ACTION_ICON, TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { finishOpTask, getOpTaskPage, startOpTask } from '#/api/mes/optask';
import { submitProdReport } from '#/api/mes/prodreport';

import { MES_OP_TASK_STATUS } from '../../order-form';
import {
  useReportFormSchema,
  useTaskGridColumns,
  useTaskGridFormSchema,
} from './data';

const selectedTask = ref<MesOpTaskApi.OpTask>();
const drawerTitle = computed(() =>
  selectedTask.value ? `报工作业 - ${selectedTask.value.taskNo}` : '报工作业',
);
const remainingQty = computed(
  () =>
    Number(selectedTask.value?.plannedQty ?? 0) -
    Number(selectedTask.value?.completedQty ?? 0) -
    Number(selectedTask.value?.scrapQty ?? 0),
);
const canStart = computed(
  () => selectedTask.value?.status === MES_OP_TASK_STATUS.READY,
);
const canReport = computed(
  () => selectedTask.value?.status === MES_OP_TASK_STATUS.RUNNING,
);
const canFinish = computed(
  () =>
    selectedTask.value?.status === MES_OP_TASK_STATUS.RUNNING &&
    remainingQty.value <= 0,
);

const [ReportForm, reportFormApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    labelClass: 'justify-start',
    labelWidth: 90,
  },
  layout: 'horizontal',
  schema: useReportFormSchema(),
  showDefaultActions: false,
});

const [TaskGrid, taskGridApi] = useVbenVxeGrid({
  formOptions: { schema: useTaskGridFormSchema() },
  gridOptions: {
    columns: useTaskGridColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) =>
          getOpTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            ...formValues,
          }),
      },
    },
    // 不再用 isCurrent：vxe 的当前行高亮由点击行触发，入口改成「明细」按钮后
    // "被高亮的行"和"真正打开抽屉的行"会分叉，反而误导操作员
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesOpTaskApi.OpTask>,
});

const [Drawer, drawerApi] = useVbenDrawer({
  class: 'w-full lg:w-3/5',
  contentClass: 'flex h-full min-h-0 flex-col overflow-hidden p-0',
  footer: false,
  async onClosed() {
    selectedTask.value = undefined;
    await reportFormApi.resetForm();
    await taskGridApi.query();
  },
});

/**
 * 选中工序任务并打开作业抽屉。
 *
 * 入口是操作列「明细」按钮而不是整行点击：整行热区会让人只想看某格内容时误开抽屉。
 * 原来为"点行不误触"写的列类型判断随入口一起删掉。
 */
async function handleOpenDetail(row: MesOpTaskApi.OpTask) {
  selectedTask.value = row;
  await reportFormApi.resetForm();
  drawerApi.open();
}

async function reloadTask() {
  if (selectedTask.value?.id)
    selectedTask.value = await getOpTaskPage({
      pageNo: 1,
      pageSize: 1,
      taskNo: selectedTask.value.taskNo,
    }).then((page) => page.list[0]);
  await taskGridApi.query();
}

async function handleStart() {
  if (!selectedTask.value?.id) return;
  const loading = ElLoading.service({ text: '正在开工...' });
  try {
    await startOpTask({ id: selectedTask.value.id });
    ElMessage.success('已开工');
    await reloadTask();
  } finally {
    loading.close();
  }
}

async function handleReport() {
  if (!selectedTask.value?.id) return;
  const { valid } = await reportFormApi.validate();
  if (!valid) return;
  const values = await reportFormApi.getValues();
  const loading = ElLoading.service({ text: '正在提交报工...' });
  try {
    await submitProdReport({
      ...values,
      opTaskId: selectedTask.value.id,
      idempotencyKey: `UI:${selectedTask.value.taskNo}:REPORT:${Date.now()}`,
    });
    ElMessage.success('报工已提交');
    await reportFormApi.resetForm();
    await reloadTask();
  } finally {
    loading.close();
  }
}

async function handleFinish() {
  if (!selectedTask.value?.id) return;
  try {
    await ElMessageBox.confirm(
      `确认完工工序任务【${selectedTask.value.taskNo}】？`,
      '工序完工',
      { type: 'warning' },
    );
  } catch {
    return;
  }
  const loading = ElLoading.service({ text: '正在完工...' });
  try {
    await finishOpTask({ id: selectedTask.value.id });
    ElMessage.success('工序已完工');
    await reloadTask();
  } finally {
    loading.close();
  }
}
</script>

<template>
  <Page auto-content-height>
    <Drawer :title="drawerTitle">
      <div
        v-if="selectedTask"
        class="flex h-full min-h-0 flex-col overflow-hidden p-4"
      >
        <ElDescriptions :column="2" border size="small" class="mb-3 shrink-0">
          <ElDescriptionsItem label="工序任务">
            {{ selectedTask.taskNo }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="工序">
            {{ selectedTask.opCode }} {{ selectedTask.opName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="计划数量">
            {{ selectedTask.plannedQty }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="已完成">
            {{ selectedTask.completedQty }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="剩余数量">
            {{ remainingQty }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="当前状态">
            {{
              getDictLabel(DICT_TYPE.MES_OP_TASK_STATUS, selectedTask.status) ||
              '-'
            }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ElAlert
          v-if="selectedTask.status !== 'RUNNING'"
          class="mb-3 shrink-0"
          :closable="false"
          show-icon
          :title="
            canStart
              ? '任务已就绪，可先开工后报工'
              : '仅生产中的任务可以提交报工'
          "
          type="warning"
        />
        <ReportForm class="min-h-0 flex-1 overflow-auto" />
        <div class="mt-4 flex shrink-0 justify-end">
          <!-- 禁用按钮保留展示并在 tooltip 里说明卡在哪一步；可点时给空串，
               TableAction 不渲染空提示，避免"仅某状态可开工"挂在能正常点的按钮上 -->
          <TableAction
            :actions="[
              {
                label: '开工',
                type: 'primary',
                icon: 'lucide:play',
                auth: ['mes:op-task:start'],
                disabled: !canStart,
                tooltip: canStart ? '' : '仅待执行状态的任务可以开工',
                onClick: handleStart,
              },
              {
                label: '提交报工',
                type: 'primary',
                icon: 'lucide:clipboard-check',
                auth: ['mes:prod-report:create'],
                disabled: !canReport,
                tooltip: canReport ? '' : '仅生产中的任务可以提交报工',
                onClick: handleReport,
              },
              {
                label: '完工',
                type: 'success',
                icon: 'lucide:check-check',
                auth: ['mes:op-task:finish'],
                disabled: !canFinish,
                tooltip: canFinish
                  ? ''
                  : '仅剩余数量为 0 的生产中任务可以完工，请先报满或走报废',
                onClick: handleFinish,
              },
            ]"
          />
        </div>
      </div>
    </Drawer>
    <TaskGrid table-title="我的工序任务">
      <template #actions="{ row }">
        <TableAction
          :actions="[
            {
              label: '明细',
              type: 'primary',
              link: true,
              icon: ACTION_ICON.VIEW,
              auth: ['mes:op-task:query'],
              onClick: handleOpenDetail.bind(null, row),
            },
          ]"
        />
      </template>
    </TaskGrid>
  </Page>
</template>
