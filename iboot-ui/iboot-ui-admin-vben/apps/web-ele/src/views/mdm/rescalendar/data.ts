import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';


import { getRangePickerDefaultProps } from '#/utils';

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
      label: '资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源编号',
      },
    },
    {
      fieldName: 'workDate',
      label: '工作日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入班次编号',
      },
    },
    {
      fieldName: 'availableMinutes',
      label: '可用产能分钟',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入可用产能分钟',
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
      label: '资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源编号',
      },
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
      label: '班次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入班次编号',
      },
    },
    {
      fieldName: 'availableMinutes',
      label: '可用产能分钟',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入可用产能分钟',
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
    {
      field: 'id',
      title: '主键',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '资源编号',
      minWidth: 120,
    },
    {
      field: 'workDate',
      title: '工作日期',
      minWidth: 120,
    },
    {
      field: 'shiftId',
      title: '班次编号',
      minWidth: 120,
    },
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
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

