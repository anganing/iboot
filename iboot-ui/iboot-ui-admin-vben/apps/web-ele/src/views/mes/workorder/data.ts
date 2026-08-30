import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWorkOrderApi } from '#/api/mes/workorder';

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
      fieldName: 'workOrderNo',
      label: '作业工单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单编号',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单号快照',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'workshopId',
      label: '车间编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入车间编号',
      },
    },
    {
      fieldName: 'wcId',
      label: '工作中心编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'plannedQty',
      label: '计划数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计划数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入已完成数量',
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
      fieldName: 'actualStartTime',
      label: '实际开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'actualEndTime',
      label: '实际结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'kitStatus',
      label: '齐套状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入乐观锁版本',
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
      label: 'MES-作业工单数据',
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
      fieldName: 'workOrderNo',
      label: '作业工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单编号',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号快照',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'workshopId',
      label: '车间编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入车间编号',
      },
    },
    {
      fieldName: 'wcId',
      label: '工作中心编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'plannedQty',
      label: '计划数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计划数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入已完成数量',
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
      fieldName: 'actualStartTime',
      label: '实际开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'actualEndTime',
      label: '实际结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'kitStatus',
      label: '齐套状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择齐套状态',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED',
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入乐观锁版本',
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
export function useGridColumns(): VxeTableGridOptions<MesWorkOrderApi.WorkOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'workOrderNo',
      title: '作业工单号',
      minWidth: 120,
    },
    {
      field: 'prodOrderNo',
      title: '生产工单号快照',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'workshopId',
      title: '车间编号',
      minWidth: 120,
    },
    {
      field: 'wcId',
      title: '工作中心编号',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目编号，0表示无项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务编号，0表示无项目任务',
      minWidth: 120,
    },
    {
      field: 'plannedQty',
      title: '计划数量',
      minWidth: 120,
    },
    {
      field: 'completedQty',
      title: '已完成数量',
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
      field: 'actualStartTime',
      title: '实际开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'actualEndTime',
      title: '实际结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'kitStatus',
      title: '齐套状态',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：PENDING、READY、RUNNING、HOLD、DONE、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'version',
      title: '乐观锁版本',
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
      field: 'prodOrderOrder_no',
      title: '生产工单order_no',
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