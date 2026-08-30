import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskApi } from '#/api/aps/task';

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
      fieldName: 'versionId',
      label: '排程版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'taskNo',
      label: '任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入任务号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入路线工序',
      },
    },
    {
      fieldName: 'opCode',
      label: '工序编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序编码',
      },
    },
    {
      fieldName: 'opName',
      label: '工序名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序名称',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂',
      },
    },
    {
      fieldName: 'workshopId',
      label: '车间',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入车间',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务',
      },
    },
    {
      fieldName: 'resId',
      label: '排定生产资源',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排定生产资源',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源编码',
      },
    },
    {
      fieldName: 'startTime',
      label: '计划开始时间',
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
      fieldName: 'endTime',
      label: '计划结束时间',
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
      fieldName: 'durationMinutes',
      label: '占用(分钟)',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入占用(分钟)',
      },
    },
    {
      fieldName: 'sequenceNo',
      label: '执行顺序',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入执行顺序',
      },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定',
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
      label: '状态',
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
      label: '排程任务数据',
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
      fieldName: 'versionId',
      label: '排程版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'taskNo',
      label: '任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入任务号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入路线工序',
      },
    },
    {
      fieldName: 'opCode',
      label: '工序编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序编码',
      },
    },
    {
      fieldName: 'opName',
      label: '工序名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序名称',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂',
      },
    },
    {
      fieldName: 'workshopId',
      label: '车间',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入车间',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务',
      },
    },
    {
      fieldName: 'resId',
      label: '排定生产资源',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排定生产资源',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源编码',
      },
    },
    {
      fieldName: 'startTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'endTime',
      label: '计划结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'durationMinutes',
      label: '占用(分钟)',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入占用(分钟)',
      },
    },
    {
      fieldName: 'sequenceNo',
      label: '执行顺序',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入执行顺序',
      },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否锁定',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<ApsTaskApi.Task>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskNo',
      title: '任务号',
      minWidth: 120,
    },
    {
      field: 'prodOrderId',
      title: '生产工单',
      minWidth: 120,
    },
    {
      field: 'prodOrderNo',
      title: '生产工单号',
      minWidth: 120,
    },
    {
      field: 'workOrderId',
      title: '作业工单',
      minWidth: 120,
    },
    {
      field: 'opTaskId',
      title: '工序任务',
      minWidth: 120,
    },
    {
      field: 'routeOpId',
      title: '路线工序',
      minWidth: 120,
    },
    {
      field: 'opCode',
      title: '工序编码',
      minWidth: 120,
    },
    {
      field: 'opName',
      title: '工序名称',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂',
      minWidth: 120,
    },
    {
      field: 'workshopId',
      title: '车间',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '排定生产资源',
      minWidth: 120,
    },
    {
      field: 'resCode',
      title: '资源编码',
      minWidth: 120,
    },
    {
      field: 'startTime',
      title: '计划开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '计划结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'durationMinutes',
      title: '占用(分钟)',
      minWidth: 120,
    },
    {
      field: 'sequenceNo',
      title: '执行顺序',
      minWidth: 120,
    },
    {
      field: 'lockedFlag',
      title: '是否锁定',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
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
      field: 'versionVersion_no',
      title: '排程版本号',
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