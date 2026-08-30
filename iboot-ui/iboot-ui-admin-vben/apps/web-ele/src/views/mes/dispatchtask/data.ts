import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispatchTaskApi } from '#/api/mes/dispatchtask';

import { getDictOptions } from '@vben/hooks';

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
      fieldName: 'dispatchNo',
      label: '派工单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入派工单号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'operatorId',
      label: '作业人员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业人员编号',
      },
    },
    {
      fieldName: 'teamId',
      label: '作业班组编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业班组编号',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产资源编号',
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入班次编号',
      },
    },
    {
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'plannedEndTime',
      label: '计划结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'MES-派工任务数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'dispatchNo',
      label: '派工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入派工单号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'operatorId',
      label: '作业人员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业人员编号',
      },
    },
    {
      fieldName: 'teamId',
      label: '作业班组编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业班组编号',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产资源编号',
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
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'plannedEndTime',
      label: '计划结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<MesDispatchTaskApi.DispatchTask>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'dispatchNo',
      title: '派工单号',
      minWidth: 120,
    },
    {
      field: 'operatorId',
      title: '作业人员编号',
      minWidth: 120,
    },
    {
      field: 'teamId',
      title: '作业班组编号',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '生产资源编号',
      minWidth: 120,
    },
    {
      field: 'shiftId',
      title: '班次编号',
      minWidth: 120,
    },
    {
      field: 'plannedStartTime',
      title: '计划开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'plannedEndTime',
      title: '计划结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED',
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
      field: 'opTaskTask_no',
      title: '工序任务task_no',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}