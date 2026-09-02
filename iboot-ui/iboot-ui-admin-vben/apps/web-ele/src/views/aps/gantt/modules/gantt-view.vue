<script lang="ts" setup>
import type { GanttGranularity, GanttGroupBy, GanttTooltip } from '../data';

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
  buildOrderTooltip,
  buildResourceTooltip,
  buildTaskTooltip,
  DEFAULT_GROUP_BY,
  formatHours,
  getColWidth,
  getScales,
  getTaskListColumns,
  GRANULARITY_OPTIONS,
  GROUP_BY_OPTIONS,
  resolveTaskBarColor,
  TASK_BAR_COLOR,
  TOOLTIP_TONE_CLASS,
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

/** 悬浮详情与光标的间距，别贴着光标以免挡住任务条本身 */
const TOOLTIP_GAP = 14;
/** 悬浮详情与容器边缘的留白 */
const TOOLTIP_EDGE = 8;

const containerRef = ref<HTMLDivElement>();
// 用 shallowRef：Gantt 实例内部持有 canvas 与场景树，深度响应式代理会拖慢渲染
const ganttInstance = shallowRef<Gantt>();
const tooltipRef = ref<HTMLDivElement>();
/** 悬浮详情：canvas 上画不出 HTML 浮层，用绝对定位的 div 跟着鼠标走 */
const tooltip = ref<{
  detail?: GanttTooltip;
  left: number;
  top: number;
  visible: boolean;
}>({ left: 0, top: 0, visible: false });
// 光标在容器内的坐标，翻转与夹边都要拿它重算，所以单独留着而不是只存最终位置
let tooltipCursor = { x: 0, y: 0 };
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
    tooltip: buildResourceTooltip(group),
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
    tooltip: buildOrderTooltip(group),
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
    tooltip: buildTaskTooltip(task, {
      delayFlag,
      editable: editable.value,
      groupBy: groupBy.value,
    }),
    raw: task,
  };
}

function hierarchyState() {
  return expanded.value ? 'expand' : 'collapse';
}

/** 任务条颜色，配色规则沉在 data.ts 里和悬浮详情共用一份口径 */
function resolveBarColor(taskRecord: any) {
  return taskRecord?.raw
    ? resolveTaskBarColor(taskRecord.raw, groupBy.value, taskRecord.delayFlag)
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
async function handleTaskBarEnter({
  record,
  event,
}: {
  event: MouseEvent;
  record: any;
}) {
  const detail = record?.tooltip as GanttTooltip | undefined;
  const box = containerRef.value?.getBoundingClientRect();
  if (!detail || !box) {
    return;
  }
  tooltipCursor = { x: event.clientX - box.left, y: event.clientY - box.top };
  // 先只换内容不显形：浮层高度随行数变化，摆位要等它按新内容量出尺寸
  tooltip.value = { ...tooltip.value, detail };
  await nextTick();
  tooltip.value = { ...tooltip.value, ...placeTooltip(), visible: true };
}

/**
 * 浮层落点。
 *
 * <p>默认贴光标右下方，那一侧放不下就翻到另一侧，最后再夹回容器内。甘特图容器是
 * overflow-hidden，越界的浮层会被直接裁掉，而最需要看详情的任务条常常正好排在最右、最下。
 */
function placeTooltip() {
  const box = containerRef.value?.getBoundingClientRect();
  const panel = tooltipRef.value?.getBoundingClientRect();
  const width = panel?.width ?? 0;
  const height = panel?.height ?? 0;
  let left = tooltipCursor.x + TOOLTIP_GAP;
  let top = tooltipCursor.y + TOOLTIP_GAP;
  if (box && left + width > box.width - TOOLTIP_EDGE) {
    left = tooltipCursor.x - TOOLTIP_GAP - width;
  }
  if (box && top + height > box.height - TOOLTIP_EDGE) {
    top = tooltipCursor.y - TOOLTIP_GAP - height;
  }
  return {
    left: Math.max(left, TOOLTIP_EDGE),
    top: Math.max(top, TOOLTIP_EDGE),
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
      <!--
        任务详情浮层：canvas 内画不出 HTML，位置由 mouseenter_task_bar 事件算出来。
        用 invisible 而不是 v-if 收起：翻转落点要先量出浮层尺寸，display:none 量出来是 0。
      -->
      <div
        ref="tooltipRef"
        class="bg-popover text-popover-foreground border-border pointer-events-none absolute z-10 w-[300px] overflow-hidden rounded-md border shadow-lg transition-opacity duration-150"
        :class="tooltip.visible ? 'opacity-100' : 'invisible opacity-0'"
        :style="{ left: `${tooltip.left}px`, top: `${tooltip.top}px` }"
      >
        <template v-if="tooltip.detail">
          <div class="flex items-start gap-2 px-3 pb-2 pt-2.5">
            <span
              class="mt-0.5 flex size-5 shrink-0 items-center justify-center rounded"
              :style="{ backgroundColor: tooltip.detail.color }"
            >
              <IconifyIcon
                :icon="tooltip.detail.icon"
                class="size-3.5 text-white"
              />
            </span>
            <div class="min-w-0 flex-1">
              <div class="truncate text-sm font-medium leading-5">
                {{ tooltip.detail.title }}
              </div>
              <div
                v-if="tooltip.detail.subtitle"
                class="text-muted-foreground truncate text-xs leading-4"
              >
                {{ tooltip.detail.subtitle }}
              </div>
            </div>
          </div>
          <div
            v-if="tooltip.detail.tags.length > 0"
            class="flex flex-wrap gap-1 px-3 pb-2"
          >
            <ElTag
              v-for="tag in tooltip.detail.tags"
              :key="tag.text"
              disable-transitions
              size="small"
              :type="tag.type"
            >
              {{ tag.text }}
            </ElTag>
          </div>
          <!-- 时间条：起止时间是移入后第一眼要找的信息，单独一行给足权重 -->
          <div
            v-if="tooltip.detail.timeline"
            class="bg-muted mx-3 mb-2 flex items-center gap-1.5 rounded px-2 py-1.5 text-xs"
          >
            <span class="font-medium">{{ tooltip.detail.timeline.start }}</span>
            <IconifyIcon
              icon="lucide:arrow-right"
              class="text-muted-foreground size-3 shrink-0"
            />
            <span class="font-medium">{{ tooltip.detail.timeline.end }}</span>
            <span class="text-muted-foreground ml-auto">
              {{ tooltip.detail.timeline.duration }}
            </span>
          </div>
          <div
            class="grid grid-cols-[auto_1fr] gap-x-3 gap-y-1 px-3 pb-2.5 text-xs"
          >
            <template v-for="row in tooltip.detail.rows" :key="row.label">
              <span class="text-muted-foreground">{{ row.label }}</span>
              <span
                class="break-all text-right"
                :class="TOOLTIP_TONE_CLASS[row.tone ?? 'default']"
              >
                {{ row.value }}
              </span>
            </template>
          </div>
          <div
            v-if="tooltip.detail.hint"
            class="border-border text-muted-foreground border-t px-3 py-1.5 text-[11px]"
          >
            {{ tooltip.detail.hint }}
          </div>
        </template>
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
