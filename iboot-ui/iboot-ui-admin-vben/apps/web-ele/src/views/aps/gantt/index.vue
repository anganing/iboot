<script lang="ts" setup>
import type { GanttGranularity } from './data';

import type { ApsTaskApi } from '#/api/aps/task';

import { computed, onBeforeUnmount, ref, shallowRef } from 'vue';

import { Page, useVbenModal } from '@vben/common-ui';
import { formatDate, formatDateTime } from '@vben/utils';

import { Gantt, TYPES } from '@visactor/vtable-gantt';
import {
  ElButton,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElMessage,
  ElRadioButton,
  ElRadioGroup,
  ElTag,
  ElTooltip,
} from 'element-plus';

import { getTaskGantt } from '#/api/aps/task';
import { RelationSelect } from '#/components/relation-select';
import AdjustForm from '#/views/aps/task/modules/adjust-form.vue';

import { APS_VERSION_RELATION_PROPS } from '../relation-config';
import {
  getColWidth,
  getScales,
  getTaskBarColor,
  getTaskListColumns,
  GRANULARITY_OPTIONS,
  TASK_BAR_COLOR,
} from './data';

/** 依赖类型到 vtable-gantt 连线类型的映射，未知类型按完工到开工兜底 */
const DEP_TYPE_MAP: Record<string, TYPES.DependencyType> = {
  FINISH_FINISH: TYPES.DependencyType.FinishToFinish,
  FINISH_START: TYPES.DependencyType.FinishToStart,
  START_START: TYPES.DependencyType.StartToStart,
};

/** 已冻结的版本不允许调整任务，和后端 validateMutable 的口径保持一致 */
const IMMUTABLE_STATUS = new Set(['PUBLISHED', 'REPLACED']);

const containerRef = ref<HTMLDivElement>();
// 用 shallowRef：Gantt 实例内部持有 canvas 与场景树，深度响应式代理会拖慢渲染
const ganttInstance = shallowRef<Gantt>();
const versionId = ref<number>();
const granularity = ref<GanttGranularity>('day');
const loading = ref(false);
const ganttData = ref<ApsTaskApi.Gantt>();

const [AdjustFormModal, adjustFormModalApi] = useVbenModal({
  connectedComponent: AdjustForm,
  destroyOnClose: true,
});

const resourceCount = computed(() => ganttData.value?.resources?.length ?? 0);
const hasData = computed(() => (ganttData.value?.taskCount ?? 0) > 0);
const editable = computed(
  () => !IMMUTABLE_STATUS.has(ganttData.value?.versionStatus ?? ''),
);
const timeWindow = computed(() => {
  const data = ganttData.value;
  if (!data?.minTime || !data?.maxTime) {
    return '-';
  }
  return `${formatDateTime(data.minTime)} ~ ${formatDateTime(data.maxTime)}`;
});

/** 把资源分组摊成 vtable-gantt 的父子记录：资源是 project 行，工序是它的子任务 */
function buildRecords(data: ApsTaskApi.Gantt) {
  return (data.resources ?? []).map((resource) => ({
    // 资源行的 key 加前缀，避免和任务编号撞到同一个 taskKeyField 命名空间
    id: `res-${resource.resId}`,
    type: TYPES.TaskType.PROJECT,
    name: resource.resName || resource.resCode || String(resource.resId),
    summary: `${resource.taskCount ?? 0} 道 / ${formatHours(resource.loadMinutes)}`,
    hierarchyState: 'collapse',
    children: (resource.tasks ?? []).map((task) => ({
      id: task.id,
      name: `${task.opCode ?? ''} ${task.opName ?? ''}`.trim(),
      summary: task.prodOrderNo ?? '',
      // vtable-gantt 只认日期字符串，后端返回的是毫秒时间戳，这里统一格式化
      startDate: formatDateTime(task.startTime),
      endDate: formatDateTime(task.endTime),
      raw: task,
    })),
  }));
}

function formatHours(minutes?: number) {
  return `${((minutes ?? 0) / 60).toFixed(1)} h`;
}

/** 依赖连线 */
function buildLinks(data: ApsTaskApi.Gantt) {
  return (data.links ?? []).map((link) => ({
    type:
      DEP_TYPE_MAP[link.depType ?? ''] ?? TYPES.DependencyType.FinishToStart,
    linkedFromTaskKey: link.fromTaskId,
    linkedToTaskKey: link.toTaskId,
  }));
}

function buildOptions(data: ApsTaskApi.Gantt) {
  const colWidth = getColWidth(granularity.value);
  return {
    records: buildRecords(data),
    taskKeyField: 'id',
    tasksShowMode: TYPES.TasksShowMode.Project_Sub_Tasks_Inline,
    projectSubTasksExpandable: true,
    overscrollBehavior: 'none' as const,
    // 时间窗左右各留一天，任务条贴着边缘时看不出起止
    minDate: formatDate(data.minTime),
    maxDate: formatDate(data.maxTime),
    headerRowHeight: 32,
    rowHeight: 34,
    underlayBackgroundColor: '#ffffff',
    taskListTable: {
      columns: getTaskListColumns(),
      tableWidth: 350,
      minTableWidth: 220,
      maxTableWidth: 560,
      theme: {
        headerStyle: {
          bgColor: '#f5f7fa',
          borderColor: '#e4e7ed',
          color: '#606266',
          fontSize: 12,
        },
        bodyStyle: {
          borderColor: '#ebeef5',
          color: '#303133',
          fontSize: 12,
        },
      },
    },
    frame: {
      outerFrameStyle: {
        borderLineWidth: 1,
        borderColor: '#e4e7ed',
        cornerRadius: 4,
      },
      verticalSplitLine: { lineColor: '#e4e7ed', lineWidth: 1 },
      horizontalSplitLine: { lineColor: '#e4e7ed', lineWidth: 1 },
      verticalSplitLineMoveable: true,
    },
    grid: {
      verticalLine: { lineColor: '#f0f2f5', lineWidth: 1 },
      horizontalLine: { lineColor: '#f0f2f5', lineWidth: 1 },
    },
    timelineHeader: {
      colWidth,
      backgroundColor: '#f5f7fa',
      verticalLine: { lineColor: '#e4e7ed', lineWidth: 1 },
      horizontalLine: { lineColor: '#e4e7ed', lineWidth: 1 },
      scales: getScales(granularity.value),
    },
    taskBar: {
      startDateField: 'startDate',
      endDateField: 'endDate',
      // 只读视图：时间由求解器与人工调整弹窗决定，拖拽会让计划和后端校验绕过
      moveable: false,
      resizable: false,
      progressAdjustable: false,
      scheduleCreatable: false,
      labelText: '{name}',
      labelTextStyle: {
        fontSize: 11,
        color: '#ffffff',
        textAlign: 'left' as const,
        padding: [0, 4, 0, 6],
      },
      barStyle: ({ taskRecord }: { taskRecord: any }) => ({
        width: 18,
        cornerRadius: 3,
        barColor: taskRecord?.raw
          ? getTaskBarColor(taskRecord.raw)
          : TASK_BAR_COLOR.planned,
      }),
      hoverBarStyle: { barOverlayColor: 'rgba(64, 158, 255, 0.25)' },
    },
    dependency: {
      links: buildLinks(data),
      linkLineStyle: { lineColor: '#909399', lineWidth: 1 },
    },
    markLine: [
      {
        date: formatDate(Date.now()),
        scrollToMarkLine: true,
        position: 'left' as const,
        style: { lineColor: '#f56c6c', lineWidth: 1, lineDash: [4, 2] },
      },
    ],
    scrollStyle: {
      visible: 'scrolling' as const,
      width: 8,
      scrollSliderColor: '#c0c4cc',
      scrollSliderCornerRadius: 4,
    },
  };
}

/** 释放实例：Gantt 持有 canvas 与事件监听，不释放会在切换版本时越堆越多 */
function releaseGantt() {
  ganttInstance.value?.release();
  ganttInstance.value = undefined;
}

function renderGantt() {
  releaseGantt();
  const data = ganttData.value;
  if (!containerRef.value || !data || !hasData.value) {
    return;
  }
  const instance = new Gantt(containerRef.value, buildOptions(data) as any);
  instance.on('click_task_bar', ({ record }: { record: any }) => {
    handleTaskClick(record);
  });
  ganttInstance.value = instance;
}

/** 点任务条打开人工调整弹窗，冻结版本只提示不拦在弹窗里 */
function handleTaskClick(record: any) {
  const task = record?.raw as ApsTaskApi.GanttTask | undefined;
  if (!task?.id) {
    return;
  }
  if (!editable.value) {
    ElMessage.warning('该版本已发布或已被替换，排程结果不允许修改');
    return;
  }
  if (task.lockedFlag || task.status === 'STARTED') {
    ElMessage.warning(`任务【${task.taskNo}】已锁定或已开工，不允许调整`);
    return;
  }
  adjustFormModalApi.setData({ id: task.id }).open();
}

async function loadGantt() {
  if (!versionId.value) {
    ganttData.value = undefined;
    releaseGantt();
    return;
  }
  loading.value = true;
  try {
    ganttData.value = await getTaskGantt(versionId.value);
    renderGantt();
  } finally {
    loading.value = false;
  }
}

/** 切换时间粒度只需重建时间轴，数据不用重新拉 */
function handleGranularityChange() {
  renderGantt();
}

/** 定位到今天的标记线 */
function handleLocateToday() {
  ganttInstance.value?.scrollToMarkLine(new Date());
}

onBeforeUnmount(releaseGantt);
</script>

<template>
  <Page auto-content-height>
    <AdjustFormModal @success="loadGantt" />
    <div class="flex h-full min-h-0 flex-col gap-3">
      <div
        class="bg-card border-border flex flex-wrap items-center gap-3 rounded border p-3"
      >
        <div class="w-[280px]">
          <RelationSelect
            v-model="versionId"
            v-bind="APS_VERSION_RELATION_PROPS"
            @change="loadGantt"
          />
        </div>
        <ElRadioGroup
          v-model="granularity"
          size="small"
          @change="handleGranularityChange"
        >
          <ElRadioButton
            v-for="item in GRANULARITY_OPTIONS"
            :key="item.value"
            :value="item.value"
          >
            {{ item.label }}
          </ElRadioButton>
        </ElRadioGroup>
        <ElTooltip content="定位到今天" placement="top">
          <ElButton :disabled="!hasData" size="small" @click="handleLocateToday">
            今天
          </ElButton>
        </ElTooltip>
        <ElTooltip content="重新加载排程结果" placement="top">
          <ElButton
            :disabled="!versionId"
            :loading="loading"
            size="small"
            @click="loadGantt"
          >
            刷新
          </ElButton>
        </ElTooltip>
        <div class="ml-auto flex items-center gap-4 text-xs">
          <span
            v-for="item in [
              { color: TASK_BAR_COLOR.planned, label: '已排定' },
              { color: TASK_BAR_COLOR.changeover, label: '含换型' },
              { color: TASK_BAR_COLOR.locked, label: '已锁定' },
              { color: TASK_BAR_COLOR.started, label: '已开工' },
            ]"
            :key="item.label"
            class="flex items-center gap-1"
          >
            <i
              class="inline-block size-3 rounded-sm"
              :style="{ backgroundColor: item.color }"
            ></i>
            {{ item.label }}
          </span>
        </div>
      </div>

      <ElDescriptions
        v-if="ganttData"
        :column="5"
        border
        class="shrink-0"
        size="small"
      >
        <ElDescriptionsItem label="排程场景">
          {{ ganttData.scenarioName || ganttData.scenarioCode || '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本号">
          V{{ ganttData.versionNo ?? '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="版本状态">
          <ElTag size="small" :type="editable ? 'primary' : 'info'">
            {{ editable ? '可调整' : '已冻结' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="资源 / 工序">
          {{ resourceCount }} 台 / {{ ganttData.taskCount ?? 0 }} 道
        </ElDescriptionsItem>
        <ElDescriptionsItem label="计划时间窗">
          {{ timeWindow }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <div class="relative min-h-0 flex-1 overflow-hidden">
        <div
          v-show="hasData"
          ref="containerRef"
          class="absolute inset-0"
          style="position: absolute"
        ></div>
        <ElEmpty
          v-if="!hasData"
          class="h-full"
          :description="
            versionId
              ? '该排程版本下暂无排程任务'
              : '请先在上方选择一个排程版本'
          "
        />
      </div>
    </div>
  </Page>
</template>
