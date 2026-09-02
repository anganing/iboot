// vtable-gantt 的 package.json exports 只声明了 "." 一个入口，深路径导入在
// moduleResolution: bundler 下无法解析，类型只能从顶层 TYPES 命名空间取
import type { TYPES } from '@visactor/vtable-gantt';

import type { ApsTaskApi } from '#/api/aps/task';

import { DICT_TYPE } from '@vben/constants';
import { getDictLabel } from '@vben/hooks';
import { formatDate } from '@vben/utils';

/** 时间粒度：车间排程既要看小时级的换型间隙，也要看整月的负荷分布，所以给三档而不是写死一档 */
export type GanttGranularity = 'day' | 'hour' | 'week';

export const GRANULARITY_OPTIONS: Array<{
  label: string;
  value: GanttGranularity;
}> = [
  { label: '小时', value: 'hour' },
  { label: '天', value: 'day' },
  { label: '周', value: 'week' },
];

/**
 * 分组维度。
 *
 * <p>资源维度回答"每台设备什么时候在干什么、哪台是瓶颈"，工单维度回答"这张工单什么时候能交、卡在哪道工序"。
 * 工艺依赖沿工单走，一张工单的工序常散落在多台设备上，资源维度下依赖链要跨行画线，所以两种维度都要留。
 */
export type GanttGroupBy = 'order' | 'resource';

/** 默认按工单：多数人打开甘特图先问"这张单什么时候能交"，看设备占用是第二步 */
export const DEFAULT_GROUP_BY: GanttGroupBy = 'order';

export const GROUP_BY_OPTIONS: Array<{
  label: string;
  value: GanttGroupBy;
}> = [
  { label: '按工单', value: 'order' },
  { label: '按资源', value: 'resource' },
];

/** 各粒度下的时间轴列宽，小时粒度列多、单列要窄，否则一天要横滚很久 */
const COL_WIDTH: Record<GanttGranularity, number> = {
  hour: 40,
  day: 60,
  week: 90,
};

const SCALE_STYLE = {
  fontSize: 12,
  fontWeight: 'normal' as const,
  color: '#606266',
  textAlign: 'center' as const,
  textBaseline: 'middle' as const,
  backgroundColor: '#f5f7fa',
  textStick: true,
};

/** 取时间轴列宽 */
export function getColWidth(granularity: GanttGranularity): number {
  return COL_WIDTH[granularity];
}

/**
 * 时间刻度。
 *
 * <p>每档都配两层：上层给日期上下文，下层给最小粒度。只配一层的话小时视图里全是 0~23，
 * 横滚两屏就分不清是哪一天了。
 */
export function getScales(
  granularity: GanttGranularity,
): TYPES.ITimelineScale[] {
  const day: TYPES.ITimelineScale = {
    unit: 'day',
    step: 1,
    style: SCALE_STYLE,
    format: ({ startDate }) =>
      `${startDate.getMonth() + 1}/${startDate.getDate()}`,
  };
  const month: TYPES.ITimelineScale = {
    unit: 'month',
    step: 1,
    style: SCALE_STYLE,
    format: ({ startDate }) =>
      `${startDate.getFullYear()}年${startDate.getMonth() + 1}月`,
  };
  switch (granularity) {
    case 'hour': {
      return [
        day,
        {
          unit: 'hour',
          step: 1,
          style: SCALE_STYLE,
          format: ({ startDate }) => `${startDate.getHours()}`,
        },
      ];
    }
    case 'week': {
      return [
        month,
        {
          unit: 'week',
          step: 1,
          startOfWeek: 'monday',
          style: SCALE_STYLE,
          format: ({ dateIndex }) => `第${dateIndex}周`,
        },
      ];
    }
    default: {
      return [month, day];
    }
  }
}

/** 任务条配色：锁定（在制）与普通计划必须一眼分得开，否则看不出哪些还能重排 */
export const TASK_BAR_COLOR = {
  changeover: '#e6a23c',
  delayed: '#f56c6c',
  locked: '#909399',
  planned: '#409eff',
  started: '#67c23a',
} as const;

/**
 * 任务条颜色。
 *
 * <p>工单维度下超期优先：一张工单只要交不上，它下面的工序先要被看见，再谈是否含换型。
 * 资源维度不看超期，那个视图关心的是设备占用与冲突。
 */
export function resolveTaskBarColor(
  task: ApsTaskApi.GanttTask,
  groupBy: GanttGroupBy,
  delayFlag?: boolean,
): string {
  if (groupBy === 'order' && delayFlag) {
    return TASK_BAR_COLOR.delayed;
  }
  if (task.status === 'STARTED' || task.status === 'DONE') {
    return TASK_BAR_COLOR.started;
  }
  if (task.lockedFlag) {
    return TASK_BAR_COLOR.locked;
  }
  return (task.changeoverMinutes ?? 0) > 0
    ? TASK_BAR_COLOR.changeover
    : TASK_BAR_COLOR.planned;
}

/** 分组行颜色：工单超期时整组标红，超期是要立刻被看见的信号，不该藏在文字里 */
function resolveGroupBarColor(delayFlag?: boolean): string {
  return delayFlag ? TASK_BAR_COLOR.delayed : TASK_BAR_COLOR.planned;
}

/** 悬浮详情里值的强调级别：超期要红、换型要黄，其余保持安静 */
export type GanttTooltipTone = 'danger' | 'default' | 'warning';

/** 强调级别对应的文字样式 */
export const TOOLTIP_TONE_CLASS: Record<GanttTooltipTone, string> = {
  danger: 'text-destructive font-medium',
  default: '',
  warning: 'text-warning font-medium',
};

/** 悬浮详情的一行 */
export interface GanttTooltipRow {
  label: string;
  tone?: GanttTooltipTone;
  value: string;
}

/** 悬浮详情标题右侧的状态标签 */
export interface GanttTooltipTag {
  text: string;
  type: 'danger' | 'info' | 'primary' | 'success' | 'warning';
}

/**
 * 悬浮详情的展示模型。
 *
 * <p>内容与渲染分开：这里只描述"展示什么"，怎么画交给 gantt-view 的浮层。
 * 工序条、资源行、工单行三种悬浮对象因此能共用同一套版式，不必各画一遍。
 */
export interface GanttTooltip {
  /** 顶部色条与图标底色，与任务条同色，移入时能确认自己看的是哪一条 */
  color: string;
  /** 底部提示，解释这条能不能点开调整 */
  hint?: string;
  icon: string;
  rows: GanttTooltipRow[];
  subtitle?: string;
  tags: GanttTooltipTag[];
  /** 时间条：起止与时长，两头都缺时不展示 */
  timeline?: { duration: string; end: string; start: string };
  title: string;
}

/** 任务状态对应的标签色，和列表里的字典色系保持同一口径 */
const STATUS_TAG_TYPE: Record<string, GanttTooltipTag['type']> = {
  CANCELLED: 'info',
  DONE: 'success',
  LOCKED: 'info',
  PLANNED: 'primary',
  RELEASED: 'warning',
  STARTED: 'success',
};

/** 分钟转小时：甘特图上所有工时口径统一到小时，分钟数在车间没有决策价值 */
export function formatHours(minutes?: number) {
  return `${((minutes ?? 0) / 60).toFixed(1)} h`;
}

/** 拼「编码 · 名称」，两者都空时返回空串交给调用方兜底 */
function joinText(...parts: Array<string | undefined>) {
  return parts.filter(Boolean).join(' · ');
}

/**
 * 时间条上的起止时间。
 *
 * <p>去掉秒、省掉重复的日期：同一天内的工序结束侧只留时分。一行里出现两遍
 * "2026-08-01 09:00:00" 会把真正要读的时分挤出视野，秒对计划也没有意义。
 */
function buildTimeline(
  start?: number | string,
  end?: number | string,
  minutes?: number,
) {
  if (!start && !end) {
    return undefined;
  }
  const sameDay =
    start && end && formatDate(start, 'MM-DD') === formatDate(end, 'MM-DD');
  return {
    duration: formatHours(minutes),
    end: end ? formatDate(end, sameDay ? 'HH:mm' : 'MM-DD HH:mm') : '-',
    start: start ? formatDate(start, 'MM-DD HH:mm') : '-',
  };
}

/** 状态标签：状态本身，加上锁定、超期这些必须一眼看到的旗标 */
function buildTaskTags(
  task: ApsTaskApi.GanttTask,
  groupBy: GanttGroupBy,
  delayFlag?: boolean,
): GanttTooltipTag[] {
  const label = task.status
    ? getDictLabel(DICT_TYPE.APS_TASK_STATUS, task.status)
    : '';
  const tags: GanttTooltipTag[] = [
    {
      text: label || task.status || '未知状态',
      type: STATUS_TAG_TYPE[task.status ?? ''] ?? 'info',
    },
  ];
  if (task.lockedFlag) {
    tags.push({ text: '已锁定', type: 'info' });
  }
  if (groupBy === 'order' && delayFlag) {
    tags.push({ text: '工单超期', type: 'danger' });
  }
  return tags;
}

/** 底部提示：点不动的时候要在移入时就说清原因，而不是等用户点完弹一个警告 */
function resolveTaskHint(task: ApsTaskApi.GanttTask, editable: boolean) {
  if (!editable) {
    return '版本已发布或已被替换，排程结果不可调整';
  }
  if (task.lockedFlag || task.status === 'STARTED') {
    return '任务已锁定或已开工，不可调整';
  }
  return '单击任务条可调整资源与计划时间';
}

/** 工序条的悬浮详情 */
export function buildTaskTooltip(
  task: ApsTaskApi.GanttTask,
  options: { delayFlag?: boolean; editable: boolean; groupBy: GanttGroupBy },
): GanttTooltip {
  const { delayFlag, editable, groupBy } = options;
  const changeoverMinutes = task.changeoverMinutes ?? 0;
  return {
    color: resolveTaskBarColor(task, groupBy, delayFlag),
    hint: resolveTaskHint(task, editable),
    icon: 'lucide:settings-2',
    rows: [
      { label: '生产工单', value: task.prodOrderNo || '-' },
      { label: '生产资源', value: joinText(task.resCode, task.resName) || '-' },
      { label: '项目', value: task.projectName || '-' },
      { label: '计划工时', value: formatHours(task.durationMinutes) },
      {
        label: '换型损失',
        tone: changeoverMinutes > 0 ? 'warning' : 'default',
        value: changeoverMinutes > 0 ? formatHours(changeoverMinutes) : '无',
      },
      {
        label: '资源内顺序',
        value: task.sequenceNo ? `第 ${task.sequenceNo} 位` : '-',
      },
    ],
    subtitle: task.taskNo ? `任务 ${task.taskNo}` : undefined,
    tags: buildTaskTags(task, groupBy, delayFlag),
    timeline: buildTimeline(task.startTime, task.endTime, task.durationMinutes),
    title: joinText(task.opCode, task.opName) || task.taskNo || '排程任务',
  };
}

/** 资源行的悬浮详情：回答这台设备被占了多久、什么时候空出来 */
export function buildResourceTooltip(
  group: ApsTaskApi.GanttResourceGroup,
): GanttTooltip {
  return {
    color: TASK_BAR_COLOR.planned,
    icon: 'lucide:cpu',
    rows: [
      { label: '工序道数', value: `${group.taskCount ?? 0} 道` },
      { label: '计划负荷', value: formatHours(group.loadMinutes) },
    ],
    subtitle: group.resCode ? `资源 ${group.resCode}` : undefined,
    tags: [],
    timeline: buildTimeline(
      group.planStartTime,
      group.planEndTime,
      group.loadMinutes,
    ),
    title: group.resName || group.resCode || String(group.resId),
  };
}

/** 工单行的悬浮详情：回答这张单能不能按期交、差多少 */
export function buildOrderTooltip(
  group: ApsTaskApi.GanttOrderGroup,
): GanttTooltip {
  return {
    color: resolveGroupBarColor(group.delayFlag),
    icon: 'lucide:clipboard-list',
    rows: [
      {
        label: '交期',
        tone: group.delayFlag ? 'danger' : 'default',
        value: group.dueDate ? formatDate(group.dueDate) : '无交期',
      },
      { label: '工序道数', value: `${group.taskCount ?? 0} 道` },
      { label: '计划工时', value: formatHours(group.loadMinutes) },
    ],
    subtitle: joinText(group.matCode, group.matName) || undefined,
    tags: group.delayFlag
      ? [{ text: `超期 ${formatHours(group.delayMinutes)}`, type: 'danger' }]
      : [{ text: '未超期', type: 'success' }],
    timeline: buildTimeline(
      group.planStartTime,
      group.planEndTime,
      group.loadMinutes,
    ),
    title: group.prodOrderNo || '未关联工单',
  };
}

/**
 * 左侧任务列表的列，vtable-gantt 自带 ListTable 渲染。
 *
 * <p>标题随维度切换：资源维度下父行是设备、次列给工单，工单维度下父行是工单、次列给交期与设备，
 * 沿用同一套标题会让人对着"资源 / 工序"读工单号。
 */
export function getTaskListColumns(groupBy: GanttGroupBy) {
  return [
    {
      field: 'name',
      title: groupBy === 'order' ? '工单 / 工序' : '资源 / 工序',
      width: 240,
      tree: true,
    },
    {
      field: 'summary',
      title: groupBy === 'order' ? '交期 / 资源' : '工单 / 负荷',
      width: 170,
    },
  ];
}
