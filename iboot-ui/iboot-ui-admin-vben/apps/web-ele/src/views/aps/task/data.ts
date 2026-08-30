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
      label: '排程版本编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'taskNo',
      label: '排程任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程任务编号',
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
      fieldName: 'workOrderId',
      label: '作业工单编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: 'MES工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入MES工序任务编号',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入路线工序编号',
      },
    },
    {
      fieldName: 'opCode',
      label: '工序编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序编码快照',
      },
    },
    {
      fieldName: 'opName',
      label: '工序名称快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序名称快照',
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
      fieldName: 'resId',
      label: '排定生产资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排定生产资源编号',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源编码快照',
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
      label: '计划占用分钟数',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计划占用分钟数',
      },
    },
    {
      fieldName: 'sequenceNo',
      label: '资源内执行顺序号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源内执行顺序号',
      },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定不可移动',
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
      label: '状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED',
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
      label: 'APS-工序排程任务数据',
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
      label: '排程版本编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'taskNo',
      label: '排程任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程任务编号',
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
      fieldName: 'workOrderId',
      label: '作业工单编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单编号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: 'MES工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入MES工序任务编号',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入路线工序编号',
      },
    },
    {
      fieldName: 'opCode',
      label: '工序编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序编码快照',
      },
    },
    {
      fieldName: 'opName',
      label: '工序名称快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序名称快照',
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
      fieldName: 'resId',
      label: '排定生产资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排定生产资源编号',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源编码快照',
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
      label: '计划占用分钟数',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计划占用分钟数',
      },
    },
    {
      fieldName: 'sequenceNo',
      label: '资源内执行顺序号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源内执行顺序号',
      },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定不可移动',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否锁定不可移动',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED',
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
      title: '排程任务编号',
      minWidth: 120,
    },
    {
      field: 'prodOrderId',
      title: '生产工单编号',
      minWidth: 120,
    },
    {
      field: 'prodOrderNo',
      title: '生产工单号快照',
      minWidth: 120,
    },
    {
      field: 'workOrderId',
      title: '作业工单编号',
      minWidth: 120,
    },
    {
      field: 'opTaskId',
      title: 'MES工序任务编号',
      minWidth: 120,
    },
    {
      field: 'routeOpId',
      title: '路线工序编号',
      minWidth: 120,
    },
    {
      field: 'opCode',
      title: '工序编码快照',
      minWidth: 120,
    },
    {
      field: 'opName',
      title: '工序名称快照',
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
      field: 'resId',
      title: '排定生产资源编号',
      minWidth: 120,
    },
    {
      field: 'resCode',
      title: '资源编码快照',
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
      title: '计划占用分钟数',
      minWidth: 120,
    },
    {
      field: 'sequenceNo',
      title: '资源内执行顺序号',
      minWidth: 120,
    },
    {
      field: 'lockedFlag',
      title: '是否锁定不可移动',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED',
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
      title: '排程版本version_no',
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