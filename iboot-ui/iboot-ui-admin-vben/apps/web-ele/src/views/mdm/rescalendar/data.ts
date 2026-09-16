import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { markRaw } from 'vue';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { RES_RELATION_PROPS, SHIFT_RELATION_PROPS } from '../relation-config';

/** 排班矩阵单元格的班次配色：按班次在结果集中的出现顺序循环取值 */
export const SHIFT_CELL_COLORS = [
  '#409eff',
  '#67c23a',
  '#e6a23c',
  '#f56c6c',
  '#909399',
  '#9254de',
  '#13c2c2',
  '#eb2f96',
];

/** 批量排班的星期选项，值与后端 DayOfWeek 对齐：1 表示周一、7 表示周日 */
export const SPAN_OPTIONS = [
  { label: '本周', value: 'week' },
  { label: '两周', value: 'twoWeeks' },
  { label: '本月', value: 'month' },
  { label: '自定义', value: 'custom' },
];

/** 批量排班的星期选项，值与后端 DayOfWeek 对齐：1 表示周一、7 表示周日 */
export const WEEKDAY_OPTIONS = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
];

/** 批量排班的表单 */
export function useBatchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'resIds',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...RES_RELATION_PROPS,
        multiple: true,
        placeholder: '请选择需要排班的生产资源',
      },
    },
    {
      fieldName: 'dateRange',
      label: '日期区间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        type: 'daterange',
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
        startPlaceholder: '开始日期',
        endPlaceholder: '结束日期',
        class: '!w-full',
      },
    },
    {
      fieldName: 'weekdays',
      label: '排班星期',
      component: 'CheckboxGroup',
      componentProps: {
        options: WEEKDAY_OPTIONS,
      },
      help: '不勾选表示区间内每天都排',
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'availableMinutes',
      label: '可用产能分钟',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入可用产能分钟',
        precision: 0,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'workDate',
      label: '工作日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
        class: '!w-full',
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'availableMinutes',
      label: '可用产能分钟',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入可用产能分钟',
        precision: 0,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'workDate',
      label: '工作日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'availableMinutes',
      label: '可用产能分钟',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入可用产能分钟',
        precision: 0,
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注',
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MdmResCalendarApi.ResCalendar>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'resCode', title: '资源编码', minWidth: 140 },
    { field: 'resName', title: '资源名称', minWidth: 160 },
    {
      field: 'workDate',
      title: '工作日期',
      minWidth: 120,
    },
    { field: 'shiftCode', title: '班次编码', minWidth: 140 },
    { field: 'shiftName', title: '班次名称', minWidth: 160 },
    {
      field: 'availableMinutes',
      title: '可用产能分钟',
      minWidth: 120,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
