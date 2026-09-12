<script lang="ts" setup>
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcResultApi } from '#/api/mes/qcresult';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { computed, nextTick, ref } from 'vue';

import { Page, useVbenDrawer } from '@vben/common-ui';

import {
  ElDescriptions,
  ElDescriptionsItem,
  ElLoading,
  ElMessage,
} from 'element-plus';

import { TableAction, useVbenVxeGrid } from '#/adapter/vxe-table';
import { batchSaveQcResult, getQcResultPage } from '#/api/mes/qcresult';
import {
  assignQcTask,
  getQcTaskPage,
  startInspectQcTask,
  submitQcTaskConclusion,
} from '#/api/mes/qctask';

import { useInspectFormColumns } from '../../qctask/data';

/** cell-click 事件参数：本页只关心行数据与列元信息 */
interface TaskCellClickEvent {
  column: {
    slots?: null | { default?: string };
    type?: null | string;
  };
  row: MesQcTaskApi.QcTask;
}

const selectedTask = ref<MesQcTaskApi.QcTask>();
const editingRows = ref<MesQcResultApi.QcResult[]>([]);

const canSubmit = computed(
  () =>
    selectedTask.value?.status === 'INSPECTING' ||
    selectedTask.value?.status === 'RESULT_POSTING',
);

const drawerTitle = computed(() =>
  selectedTask.value
    ? `检验录入 - ${selectedTask.value.taskNo ?? ''}`
    : '检验录入',
);

const [TaskGrid, taskGridApi] = useVbenVxeGrid({
  formOptions: {},
  gridOptions: {
    columns: useInspectFormColumns(),
    height: 'auto',
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }) =>
          getQcTaskPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            status: 'CREATED',
          }),
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { refresh: true, search: true, zoom: true },
  } as VxeTableGridOptions<MesQcTaskApi.QcTask>,
});

const [ResultGrid, resultGridApi] = useVbenVxeGrid({
  gridOptions: {
    columns: [
      { type: 'seq', title: '序号', width: 60 },
      { field: 'itemCode', title: '检验项编码', minWidth: 130 },
      { field: 'itemName', title: '检验项名称', minWidth: 160 },
      { field: 'sampleNo', title: '样本', width: 80 },
      {
        field: 'actualValue',
        title: '实测值',
        minWidth: 140,
        slots: { edit: 'actual-value' },
      },
      {
        field: 'passFlag',
        title: '是否合格',
        width: 100,
        formatter: ({ cellValue }) => (cellValue ? '合格' : '待判/不合格'),
      },
      { field: 'remark', title: '备注', minWidth: 160 },
    ],
    height: 'auto',
    keepSource: true,
    editConfig: { mode: 'cell', trigger: 'click' },
    pagerConfig: { enabled: false },
    // 抽屉打开后由 handleTaskClick 显式 query，禁用挂载时自动加载避免双发请求
    proxyConfig: {
      autoLoad: false,
      ajax: {
        query: async () => {
          if (!selectedTask.value?.id) return { list: [], total: 0 };
          return getQcResultPage({
            pageNo: 1,
            pageSize: 1000,
            taskId: selectedTask.value.id,
          });
        },
      },
    },
    rowConfig: { keyField: 'id', isHover: true },
    toolbarConfig: { custom: true, refresh: true, search: false, zoom: true },
  } as VxeTableGridOptions<MesQcResultApi.QcResult>,
});

const [Drawer, drawerApi] = useVbenDrawer({
  destroyOnClose: true,
  footer: false,
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      selectedTask.value = undefined;
      editingRows.value = [];
      taskGridApi.query();
    }
  },
});

function handleTaskClick({ column, row }: TaskCellClickEvent) {
  // 命中 checkbox 列、序号列或 #actions 操作列时不承载“选中任务”语义，不打开抽屉
  if (
    column.type === 'checkbox' ||
    column.type === 'seq' ||
    column.slots?.default === 'actions'
  ) {
    return;
  }
  selectedTask.value = row;
  editingRows.value = [];
  drawerApi.open();
  // destroyOnClose 抽屉每次打开重新挂载结果 Grid，等挂载完成后再查询
  nextTick(() => {
    resultGridApi.query();
  });
}

async function handleAssign() {
  if (!selectedTask.value) return;
  await assignQcTask(
    selectedTask.value.id!,
    selectedTask.value.assignedInspectorId || 1,
  );
  ElMessage.success('检验员已指派');
  taskGridApi.query();
}

/** 保存右侧已填写的实测值格子；后端逐格判定并推进任务。 */
async function handleSaveResult() {
  if (!selectedTask.value?.id) return;
  const changedRows = editingRows.value.filter((row) => row.actualValue);
  if (changedRows.length === 0) {
    ElMessage.warning('请先录入实测值');
    return;
  }
  const loading = ElLoading.service({ text: '正在保存检验结果...' });
  try {
    await batchSaveQcResult({
      taskId: selectedTask.value.id,
      // 作业页暂按任务上已指派检验员落款；后续接当前用户上下文后替换
      inspectorId: selectedTask.value.assignedInspectorId || 1,
      items: changedRows.map((row) => ({
        itemId: row.itemId!,
        sampleNo: row.sampleNo!,
        actualValue: row.actualValue,
      })),
    });
    editingRows.value = [];
    ElMessage.success('检验结果已保存');
    resultGridApi.query();
    taskGridApi.query();
  } finally {
    loading.close();
  }
}

async function handleStartInspect() {
  if (!selectedTask.value) return;
  await startInspectQcTask(selectedTask.value.id!);
  selectedTask.value.status = 'INSPECTING';
  ElMessage.success('检验已开始');
  taskGridApi.query();
}

async function handleSubmit() {
  if (!selectedTask.value) return;
  const loading = ElLoading.service({ text: '正在提交检验结论...' });
  try {
    await submitQcTaskConclusion({
      id: selectedTask.value.id!,
      conclusion: 'QUALIFIED',
      qtyQualified: selectedTask.value.sampleQty || 0,
      qtyRejected: 0,
    });
    ElMessage.success('结论已提交');
    taskGridApi.query();
  } finally {
    loading.close();
  }
}

function handleActualValueChange(row: MesQcResultApi.QcResult, value: string) {
  row.actualValue = value;
  if (!editingRows.value.some((item) => item.id === row.id)) {
    editingRows.value.push(row);
  }
}
</script>

<template>
  <Page auto-content-height>
    <TaskGrid table-title="待检任务" @cell-click="handleTaskClick" />
    <Drawer
      :title="drawerTitle"
      class="w-full lg:w-4/5"
      content-class="flex h-full min-h-0 flex-col overflow-hidden p-0"
    >
      <section class="flex h-full min-h-0 w-full flex-col gap-2 p-2">
        <ElDescriptions
          v-if="selectedTask"
          border
          class="shrink-0"
          size="small"
          :column="3"
        >
          <ElDescriptionsItem label="质检任务号">
            {{ selectedTask.taskNo }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="物料">
            {{ selectedTask.matCode }} {{ selectedTask.matName }}
          </ElDescriptionsItem>
          <ElDescriptionsItem label="抽样数量">
            {{ selectedTask.sampleQty }}
          </ElDescriptionsItem>
        </ElDescriptions>
        <ResultGrid class="min-h-0 flex-1" table-title="检验结果">
          <template #actual-value="{ row }">
            <input
              :value="row.actualValue"
              class="h-7 w-full rounded border border-gray-200 px-2 text-sm"
              @change="
                handleActualValueChange(
                  row,
                  ($event.target as HTMLInputElement).value,
                )
              "
            />
          </template>
        </ResultGrid>
        <div class="flex shrink-0 justify-end">
          <TableAction
            :actions="[
              {
                label: '指派',
                type: 'primary',
                auth: ['mes:qc-task:assign'],
                disabled: selectedTask?.status !== 'CREATED',
                onClick: handleAssign,
              },
              {
                label: '开始检验',
                type: 'primary',
                auth: ['mes:qc-task:start-inspect'],
                disabled: selectedTask?.status !== 'ASSIGNED',
                onClick: handleStartInspect,
              },
              {
                label: '保存结果',
                type: 'primary',
                auth: ['mes:qc-result:batch-save'],
                disabled: selectedTask?.status !== 'INSPECTING',
                onClick: handleSaveResult,
              },
              {
                label: '提交结论',
                type: 'primary',
                auth: ['mes:qc-task:submit-conclusion'],
                disabled: !canSubmit,
                onClick: handleSubmit,
              },
            ]"
          />
        </div>
      </section>
    </Drawer>
  </Page>
</template>
