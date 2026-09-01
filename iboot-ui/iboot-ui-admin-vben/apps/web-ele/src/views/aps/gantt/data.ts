import type { ITimelineScale } from '@visactor/vtable-gantt/es/ts-types';

import type { ApsTaskApi } from '#/api/aps/task';

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
export function getScales(granularity: GanttGranularity): ITimelineScale[] {
  const day: ITimelineScale = {
    unit: 'day',
    step: 1,
    style: SCALE_STYLE,
    format: ({ startDate }) =>
      `${startDate.getMonth() + 1}/${startDate.getDate()}`,
  };
  const month: ITimelineScale = {
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
  locked: '#909399',
  planned: '#409eff',
  started: '#67c23a',
} as const;

/** 按任务状态取任务条颜色 */
export function getTaskBarColor(task: ApsTaskApi.GanttTask): string {
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

/** 左侧任务列表的列，vtable-gantt 自带 ListTable 渲染 */
export function getTaskListColumns() {
  return [
    {
      field: 'name',
      title: '资源 / 工序',
      width: 220,
      tree: true,
    },
    {
      field: 'summary',
      title: '工单 / 负荷',
      width: 130,
    },
  ];
}
