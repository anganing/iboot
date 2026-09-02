<script lang="ts" setup>
import type { GanttGranularity, GanttGroupBy } from '../data';

import type { ApsTaskApi } from '#/api/aps/task';

import {
  computed,
  nextTick,
  onBeforeUnmount,
  ref,
  shallowRef,
  watch,
} from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { formatDate, formatDateTime } from '@vben/utils';

import { Gantt, TYPES, VRender } from '@visactor/vtable-gantt';
import {
  ElButton,
  ElDescriptions,
  ElDescriptionsItem,
  ElEmpty,
  ElLink,
  ElMessage,
  ElRadioButton,
  ElRadioGroup,
  ElTag,
  ElTooltip,
} from 'element-plus';

import { getTaskGantt } from '#/api/aps/task';
import AdjustForm from '#/views/aps/task/modules/adjust-form.vue';

import {
  DEFAULT_GROUP_BY,
  getColWidth,
  getScales,
  getTaskBarColor,
  getTaskListColumns,
  GRANULARITY_OPTIONS,
  GROUP_BY_OPTIONS,
  TASK_BAR_COLOR,
} from '../data';

/**
 * 排程甘特图视图。
 *
 * <p>独立成组件是为了让甘特图页面和排程版本列表的全屏弹窗共用同一份渲染逻辑，
 * 版本从哪里选由外层决定，这里只负责按 versionId 取数与画图。
 */
const props = defineProps<{
  /** 排程版本编号，为空时只显示空状态 */
  versionId?: number;
}>();

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
/** 悬浮详情：canvas 上画不出 HTML 浮层，用绝对定位的 div 跟着鼠标走 */
const tooltip = ref<{
  left: number;
  rows: Array<{ label: string; value: string }>;
  title: string;
  top: number;
  visible: boolean;
}>({ left: 0, rows: [], title: '', top: 0, visible: false });
const granularity = ref<GanttGranularity>('day');
const groupBy = ref<GanttGroupBy>(DEFAULT_GROUP_BY);
// 默认收起：一键展开后行数等于任务数，几百道工序的版本会拉出很长一屏
const expanded = ref(false);
const loading = ref(false);
const ganttData = ref<ApsTaskApi.Gantt>();

const [AdjustFormModal, adjustFormModalApi] = useVbenModal({
  connectedComponent: AdjustForm,
  destroyOnClose: true,
});

const resourceCount = computed(
  () => ganttData.value?.resourceGroups?.length ?? 0,
);
const orderCount = computed(() => ganttData.value?.orderGroups?.length ?? 0);
const delayedOrderCount = computed(
  () =>
    ganttData.value?.orderGroups?.filter((item) => item.delayFlag).length ?? 0,
);
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

/** 按选中维度把平铺任务分到父行下 */
function buildRecords(data: ApsTaskApi.Gantt) {
  return groupBy.value === 'order' ? buildOrderRecords(data) : buildResourceRecords(data);
}

/** 资源维度：一台设备一个父行，回答"每台设备什么时候在干什么" */
function buildResourceRecords(data: ApsTaskApi.Gantt) {
  const taskMap = groupTasks(data, (task) => task.resId);
  return (data.resourceGroups ?? []).map((group) => ({
    // 父行的 key 加前缀，避免和任务编号撞到同一个 taskKeyField 命名空间
    id: `res-${group.resId}`,
    type: TYPES.TaskType.PROJECT,
    name: group.resName || group.resCode || String(group.resId),
    summary: `${group.taskCount ?? 0} 道 / ${formatHours(group.loadMinutes)}`,
    hierarchyState: hierarchyState(),
    tooltip: {
      title: group.resName || group.resCode || String(group.resId),
      rows: [
        { label: '资源编码', value: group.resCode || '-' },
        { label: '工序道数', value: `${group.taskCount ?? 0} 道` },
        { label: '计划负荷', value: formatHours(group.loadMinutes) },
        {
          label: '占用区间',
          value: formatRange(group.planStartTime, group.planEndTime),
        },
      ],
    },
    children: (taskMap.get(group.resId) ?? []).map((task) =>
      toTaskRecord(task, task.prodOrderNo ?? ''),
    ),
  }));
}

/** 工单维度：一张工单一个父行，回答"这张工单什么时候能交、卡在哪道工序" */
function buildOrderRecords(data: ApsTaskApi.Gantt) {
  const taskMap = groupTasks(data, (task) => task.prodOrderId);
  return (data.orderGroups ?? []).map((group) => ({
    id: `order-${group.prodOrderId}`,
    type: TYPES.TaskType.PROJECT,
    name: group.prodOrderNo
      ? [group.prodOrderNo, group.matCode].filter(Boolean).join(' · ')
      : '未关联工单',
    summary: group.dueDate
      ? `交期 ${formatDate(group.dueDate)}${group.delayFlag ? ' 超期' : ''}`
      : '无交期',
    hierarchyState: hierarchyState(),
    // 工单超期时整组任务条标红：超期是要立刻被看见的信号，不该藏在文字里
    delayFlag: group.delayFlag,
    tooltip: {
      title: group.prodOrderNo || '未关联工单',
      rows: [
        { label: '生产物料', value: joinText(group.matCode, group.matName) },
        { label: '交期', value: group.dueDate ? formatDateTime(group.dueDate) : '无交期' },
        {
          label: '计划完工',
          value: group.planEndTime ? formatDateTime(group.planEndTime) : '-',
        },
        {
          label: '交期风险',
          value: group.delayFlag
            ? `超期 ${formatHours(group.delayMinutes)}`
            : '未超期',
        },
        { label: '工序道数', value: `${group.taskCount ?? 0} 道` },
        { label: '计划工时', value: formatHours(group.loadMinutes) },
      ],
    },
    children: (taskMap.get(group.prodOrderId) ?? []).map((task) =>
      toTaskRecord(task, task.resName || task.resCode || '', group.delayFlag),
    ),
  }));
}

/** 把平铺任务按分组键归组，顺序沿用后端返回的资源、开始时间排序 */
function groupTasks(
  data: ApsTaskApi.Gantt,
  keyGetter: (task: ApsTaskApi.GanttTask) => number | undefined,
) {
  const map = new Map<number, ApsTaskApi.GanttTask[]>();
  for (const task of data.tasks ?? []) {
    const key = keyGetter(task);
    if (key === undefined || key === null) {
      continue;
    }
    const bucket = map.get(key);
    if (bucket) {
      bucket.push(task);
    } else {
      map.set(key, [task]);
    }
  }
  // 工单维度下同一工单的工序散落在多台设备上，按开始时间重排才是工艺顺序
  for (const bucket of map.values()) {
    bucket.sort((left, right) =>
      String(left.startTime ?? '').localeCompare(String(right.startTime ?? '')),
    );
  }
  return map;
}

function toTaskRecord(
  task: ApsTaskApi.GanttTask,
  summary: string,
  delayFlag?: boolean,
) {
  return {
    id: task.id,
    name: `${task.opCode ?? ''} ${task.opName ?? ''}`.trim(),
    // 折叠态一行内联多条任务，只标得下工序编码；展开态才用全称
    shortName: task.opCode ?? task.taskNo ?? '',
    summary,
    // vtable-gantt 只认日期字符串，后端返回的是毫秒时间戳，这里统一格式化
    startDate: formatDateTime(task.startTime),
    endDate: formatDateTime(task.endTime),
    delayFlag,
    tooltip: {
      title: `${task.opCode ?? ''} ${task.opName ?? ''}`.trim() || '排程任务',
      rows: [
        { label: '任务编号', value: task.taskNo || '-' },
        { label: '生产工单', value: task.prodOrderNo || '-' },
        {
          label: '生产资源',
          value: joinText(task.resCode, task.resName),
        },
        { label: '项目', value: task.projectName || '-' },
        {
          label: '计划时间',
          value: formatRange(task.startTime, task.endTime),
        },
        { label: '计划工时', value: formatHours(task.durationMinutes) },
        {
          label: '换型损失',
          value: (task.changeoverMinutes ?? 0) > 0
            ? formatHours(task.changeoverMinutes)
            : '无',
        },
        { label: '资源内顺序', value: `第 ${task.sequenceNo ?? '-'} 位` },
        { label: '任务状态', value: getTaskStatusText(task) },
      ],
    },
    raw: task,
  };
}

function hierarchyState() {
  return expanded.value ? 'expand' : 'collapse';
}

/**
 * 任务条颜色。
 *
 * <p>工单维度下超期优先：一张工单只要交不上，它下面的工序先要被看见，再谈是否含换型。
 * 资源维度不看超期，那个视图关心的是设备占用与冲突。
 */
function resolveBarColor(taskRecord: any) {
  if (groupBy.value === 'order' && taskRecord?.delayFlag) {
    return TASK_BAR_COLOR.delayed;
  }
  return taskRecord?.raw
    ? getTaskBarColor(taskRecord.raw)
    : TASK_BAR_COLOR.planned;
}

/**
 * 自绘任务条标签，让文字在色块内换行。
 *
 * <p>vtable-gantt 内置标签只支持单行：放不下就截断成 "OP010 …"，或者靠
 * orientHandleWithOverflow 甩到条外压住相邻任务条。这里返回一个 Group 让它只画色块，
 * 文字用 vrender 的 Text 自己画，靠 whiteSpace: 'normal' + maxLineWidth 换行，
 * heightLimit 兜住不溢出行高。折叠态一行内联着整个分组的多条任务，只标编码。
 */
function renderTaskBarLabel({
  width,
  height,
  taskRecord,
}: {
  height: number;
  taskRecord: any;
  width: number;
}) {
  const paddingX = 4;
  const container = new VRender.Group({
    x: 0,
    y: 0,
    width,
    height,
    cornerRadius: 3,
    fill: resolveBarColor(taskRecord),
    clip: true,
  });
  // 分组父行永远用全名（它只在展开态出现）；工序条折叠态只标编码，展开态标编码 + 名称
  const isGroupRow = taskRecord?.type === TYPES.TaskType.PROJECT;
  const text =
    isGroupRow || expanded.value
      ? taskRecord?.name || ''
      : taskRecord?.shortName || '';
  container.add(
    new VRender.Text({
      x: paddingX,
      y: height / 2,
      text,
      fontSize: 11,
      fill: '#ffffff',
      textAlign: 'left',
      textBaseline: 'middle',
      maxLineWidth: Math.max(width - 2 * paddingX, 1),
      // whiteSpace: 'normal' 才走多行布局，wordBreak 让中文按字断行
      whiteSpace: 'normal',
      wordBreak: 'break-all',
      heightLimit: height,
      // 必须给字符串：传 true 时 vrender 会把布尔当后缀，截断后拼出 "…VPtrue" 这种鬼东西
      ellipsis: '...',
      pickable: false,
    } as any),
  );
  return { rootContainer: container, renderDefaultBar: false, renderDefaultText: false };
}

function formatHours(minutes?: number) {
  return `${((minutes ?? 0) / 60).toFixed(1)} h`;
}

/** 拼「编码 · 名称」，两者都空时给占位 */
function joinText(...parts: Array<string | undefined>) {
  return parts.filter(Boolean).join(' · ') || '-';
}

/** 时间区间，缺一头就只显示已有的那一头 */
function formatRange(start?: number | string, end?: number | string) {
  if (!start && !end) {
    return '-';
  }
  return `${start ? formatDateTime(start) : '-'} ~ ${end ? formatDateTime(end) : '-'}`;
}

/** 任务状态中文：走字典而不是在这里硬编码映射表 */
function getTaskStatusText(task: ApsTaskApi.GanttTask) {
  const label = task.status
    ? getDictLabel(DICT_TYPE.APS_TASK_STATUS, task.status)
    : '';
  const flags = [label || task.status || '-'];
  if (task.lockedFlag) {
    flags.push('已锁定');
  }
  return flags.join(' / ');
}

/** 按天平移时间轴边界，用来给时间窗留出余量 */
function shiftDate(time: number | string | undefined, days: number) {
  const base = time ? new Date(time) : new Date();
  base.setDate(base.getDate() + days);
  return formatDate(base);
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
    // 时间窗左右各留出余量：左边一天让首条任务不贴边，右边留五天，
    // 否则最后一条任务的条会顶在外框上，看不出它到底排到哪天结束
    minDate: shiftDate(data.minTime, -1),
    maxDate: shiftDate(data.maxTime, 5),
    headerRowHeight: 32,
    // 展开态行高给足：自绘标签在窄条里要换行，34 只够一行
    rowHeight: expanded.value ? 44 : 34,
    underlayBackgroundColor: '#ffffff',
    taskListTable: {
      columns: getTaskListColumns(groupBy.value),
      tableWidth: 410,
      minTableWidth: 260,
      maxTableWidth: 620,
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
      // 用 customLayout 自绘标签，换掉内置的单行截断：内置实现只有"截断"和"甩到条外"两种，
      // 前者读不到全称，后者会压住同一资源上首尾相接的相邻工序。这里让文字在色块内换行，
      // 窄条也能读全，条宽仍由计划时间决定，不必为文字统一拉宽列宽浪费横向空间。
      customLayout: renderTaskBarLabel,
      barStyle: ({ taskRecord }: { taskRecord: any }) => ({
        width: expanded.value ? 30 : 18,
        cornerRadius: 3,
        barColor: resolveBarColor(taskRecord),
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
  hideTooltip();
  ganttInstance.value?.release();
  ganttInstance.value = undefined;
}

/**
 * 渲染甘特图。
 *
 * <p>先 nextTick 再建实例：容器由 hasData 控制显隐，同一个 tick 里它还是 display:none，
 * canvas 会按 1px 宽建出来，虽然 vtable 自带的 ResizeObserver 事后能救回来，但会闪一下。
 */
async function renderGantt() {
  releaseGantt();
  await nextTick();
  const data = ganttData.value;
  if (!containerRef.value || !data || !hasData.value) {
    return;
  }
  const instance = new Gantt(containerRef.value, buildOptions(data) as any);
  instance.on('click_task_bar', ({ record }: { record: any }) => {
    handleTaskClick(record);
  });
  instance.on('mouseenter_task_bar', handleTaskBarEnter as any);
  instance.on('mouseleave_task_bar', hideTooltip);
  ganttInstance.value = instance;
}

/**
 * 悬浮显示任务详情。
 *
 * <p>canvas 里画不出 HTML 浮层，所以用一个绝对定位的 div 跟着鼠标走。位置取自原生事件的
 * clientX/clientY 再换算成容器内坐标：Gantt 的 offset 是画布坐标，横向滚动后会和屏幕位置错开。
 */
function handleTaskBarEnter({ record, event }: { event: MouseEvent; record: any }) {
  const detail = record?.tooltip;
  const box = containerRef.value?.getBoundingClientRect();
  if (!detail || !box) {
    return;
  }
  tooltip.value = {
    // 往右下偏一点，避免浮层压在光标下面挡住任务条本身
    left: event.clientX - box.left + 14,
    top: event.clientY - box.top + 14,
    rows: detail.rows ?? [],
    title: detail.title ?? '',
    visible: true,
  };
}

function hideTooltip() {
  tooltip.value = { ...tooltip.value, visible: false };
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
  if (!props.versionId) {
    ganttData.value = undefined;
    releaseGantt();
    return;
  }
  loading.value = true;
  try {
    ganttData.value = await getTaskGantt(props.versionId);
    await renderGantt();
  } finally {
    loading.value = false;
  }
}

/** 切换时间粒度、分组维度、展开态都只影响渲染，数据不用重新拉 */
function handleGranularityChange() {
  renderGantt();
}

/**
 * 切换分组维度。
 *
 * <p>顺带把展开态收回去：资源维度展开着切到工单维度，行数会从"设备数"跳到"工单数 x 工序数"，
 * 视口会被一屏任务条冲掉，先给个可纵览的起点更稳。
 */
function handleGroupByChange() {
  expanded.value = false;
  renderGantt();
}

/**
 * 一键展开、收起所有分组。
 *
 * <p>用重建记录而不是逐行 toggleHierarchyState：vtable-gantt 的展开态存在记录的 hierarchyState 上，
 * 逐行切换要同时同步左侧 ListTable 与任务条场景树，重建一次记录反而更稳。
 *
 * <p>顺带一个坑：父行收起时 vtable-gantt 会把依赖连线端点画在父行上（scenegraph/dependency-link.js
 * 按 hierarchyState !== 'expand' 走父行 index），所以只有展开后看到的才是真实的工序间连线。
 */
function handleToggleExpandAll() {
  expanded.value = !expanded.value;
  renderGantt();
}

/** 定位到今天的标记线 */
function handleLocateToday() {
  ganttInstance.value?.scrollToMarkLine(new Date());
}

// 外层换版本就重新取数：弹窗形态下组件挂载时版本已经确定，immediate 才能画出第一屏
watch(() => props.versionId, loadGantt, { immediate: true });

onBeforeUnmount(releaseGantt);
</script>

<template>
  <div class="flex h-full min-h-0 flex-col gap-3">
    <AdjustFormModal @success="loadGantt" />
    <div
      class="bg-card border-border flex flex-wrap items-center gap-3 rounded border p-3"
    >
      <slot name="toolbar-prefix"></slot>
      <ElLink
        :disabled="!hasData"
        :underline="false"
        type="primary"
        @click="handleToggleExpandAll"
      >
        {{ expanded ? '收起' : '展开' }}
        <IconifyIcon
          icon="lucide:chevron-down"
          class="ml-0.5 transition-transform duration-300"
          :class="{ 'rotate-180': expanded }"
        />
      </ElLink>
      <ElRadioGroup
        v-model="groupBy"
        size="small"
        @change="handleGroupByChange"
      >
        <ElRadioButton
          v-for="item in GROUP_BY_OPTIONS"
          :key="item.value"
          :value="item.value"
        >
          {{ item.label }}
        </ElRadioButton>
      </ElRadioGroup>
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
            ...(groupBy === 'order'
              ? [{ color: TASK_BAR_COLOR.delayed, label: '超期' }]
              : []),
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
      :column="6"
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
      <ElDescriptionsItem label="工单 / 超期">
        {{ orderCount }} 张 /
        <ElTag
          v-if="delayedOrderCount > 0"
          size="small"
          type="danger"
          class="ml-1"
        >
          {{ delayedOrderCount }} 张超期
        </ElTag>
        <span v-else>无超期</span>
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
      <!-- 任务详情浮层：canvas 内画不出 HTML，位置由 mouseenter_task_bar 事件算出来 -->
      <div
        v-if="tooltip.visible"
        class="bg-card border-border pointer-events-none absolute z-10 max-w-[340px] rounded border p-2 text-xs shadow-lg"
        :style="{ left: `${tooltip.left}px`, top: `${tooltip.top}px` }"
      >
        <div class="mb-1 font-medium">{{ tooltip.title }}</div>
        <div
          v-for="row in tooltip.rows"
          :key="row.label"
          class="flex gap-2 leading-5"
        >
          <span class="text-muted-foreground shrink-0">{{ row.label }}</span>
          <span class="break-all">{{ row.value }}</span>
        </div>
      </div>
      <ElEmpty
        v-if="!hasData"
        class="h-full"
        :description="
          versionId ? '该排程版本下暂无排程任务' : '请先选择一个排程版本'
        "
      />
    </div>
  </div>
</template>
