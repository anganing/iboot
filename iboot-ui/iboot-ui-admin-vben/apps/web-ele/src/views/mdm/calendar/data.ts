import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

const EXCEPTION_TYPE_OPTIONS = getDictOptions(
  'mdm_calendar_exception_type',
  'string',
);

/** 新增/修改日历主档的表单 */
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
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入日历名称',
        maxLength: 128,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
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
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入编码',
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MdmCalendarApi.Calendar>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '编码', minWidth: 120 },
    { field: 'name', title: '名称', minWidth: 180 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 160 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 340,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 规则列表的字段 */
export function useRuleGridColumns(): VxeTableGridOptions<MdmCalendarApi.CalendarRule>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    {
      field: 'dayOfWeek',
      title: '星期几',
      minWidth: 100,
      formatter: ({ cellValue }) =>
        ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][
          cellValue
        ] ?? cellValue,
    },
    { field: 'shiftCode', title: '班次编码', minWidth: 120 },
    { field: 'shiftName', title: '班次名称', minWidth: 140 },
    { field: 'availableMinutes', title: '可用分钟数', minWidth: 120 },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 例外列表的字段 */
export function useExceptionGridColumns(): VxeTableGridOptions<MdmCalendarApi.CalendarException>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'exceptionDate', title: '例外日期', minWidth: 120 },
    {
      field: 'exceptionType',
      title: '例外类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: 'mdm_calendar_exception_type' },
      },
    },
    { field: 'shiftName', title: '班次', minWidth: 120 },
    { field: 'availableMinutes', title: '可用分钟数', minWidth: 110 },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 例外类型选项 */
export function getExceptionTypeOptions() {
  return EXCEPTION_TYPE_OPTIONS;
}

/** 星期选项 */
export function getDayOfWeekOptions() {
  return [1, 2, 3, 4, 5, 6, 7].map((value) => ({
    label: ['', '周一', '周二', '周三', '周四', '周五', '周六', '周日'][value],
    value,
  }));
}
