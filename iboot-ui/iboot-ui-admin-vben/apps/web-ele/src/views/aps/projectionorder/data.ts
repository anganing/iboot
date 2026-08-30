import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionOrderApi } from '#/api/aps/projectionorder';

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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：DEMAND、PROD_ORDER、OP_TASK',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源类型：DEMAND、PROD_ORDER、OP_TASK',
      },
    },
    {
      fieldName: 'sourceId',
      label: 'MES来源对象编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入MES来源对象编号',
      },
    },
    {
      fieldName: 'sourceNo',
      label: 'MES来源对象单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入MES来源对象单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号，0表示尚未生成工单',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单编号，0表示尚未生成工单',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示尚未生成作业工单',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号，0表示尚未生成作业工单',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序投影',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号，0表示非工序投影',
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
      label: '车间编号，0表示未指定',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入车间编号，0表示未指定',
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
      fieldName: 'matId',
      label: '生产物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产物料编号',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编码快照',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序编号，0表示需求或整单',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入路线工序编号，0表示需求或整单',
      },
    },
    {
      fieldName: 'resId',
      label: '当前资源编号，0表示未分配',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前资源编号，0表示未分配',
      },
    },
    {
      fieldName: 'qty',
      label: '计划数量或剩余数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计划数量或剩余数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入已完成数量快照',
      },
    },
    {
      fieldName: 'dueDate',
      label: '交期',
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
      fieldName: 'priority',
      label: '优先级',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级',
      },
    },
    {
      fieldName: 'currentStatus',
      label: '来源对象状态快照',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
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
      fieldName: 'lockedFlag',
      label: '是否已开工或业务锁定',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
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
      fieldName: 'snapshotTime',
      label: '投影快照时间',
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
      label: 'APS-MES需求工单任务投影数据',
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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：DEMAND、PROD_ORDER、OP_TASK',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源类型：DEMAND、PROD_ORDER、OP_TASK',
      },
    },
    {
      fieldName: 'sourceId',
      label: 'MES来源对象编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入MES来源对象编号',
      },
    },
    {
      fieldName: 'sourceNo',
      label: 'MES来源对象单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入MES来源对象单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号，0表示尚未生成工单',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单编号，0表示尚未生成工单',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示尚未生成作业工单',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单编号，0表示尚未生成作业工单',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序投影',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号，0表示非工序投影',
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
      label: '车间编号，0表示未指定',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入车间编号，0表示未指定',
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
      fieldName: 'matId',
      label: '生产物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产物料编号',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编码快照',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序编号，0表示需求或整单',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入路线工序编号，0表示需求或整单',
      },
    },
    {
      fieldName: 'resId',
      label: '当前资源编号，0表示未分配',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前资源编号，0表示未分配',
      },
    },
    {
      fieldName: 'qty',
      label: '计划数量或剩余数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计划数量或剩余数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入已完成数量快照',
      },
    },
    {
      fieldName: 'dueDate',
      label: '交期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级',
      },
    },
    {
      fieldName: 'currentStatus',
      label: '来源对象状态快照',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源对象状态快照',
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
      fieldName: 'lockedFlag',
      label: '是否已开工或业务锁定',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否已开工或业务锁定',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<ApsProjectionOrderApi.ProjectionOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'sourceType',
      title: '来源类型：DEMAND、PROD_ORDER、OP_TASK',
      minWidth: 120,
    },
    {
      field: 'sourceId',
      title: 'MES来源对象编号',
      minWidth: 120,
    },
    {
      field: 'sourceNo',
      title: 'MES来源对象单号',
      minWidth: 120,
    },
    {
      field: 'prodOrderId',
      title: '生产工单编号，0表示尚未生成工单',
      minWidth: 120,
    },
    {
      field: 'workOrderId',
      title: '作业工单编号，0表示尚未生成作业工单',
      minWidth: 120,
    },
    {
      field: 'opTaskId',
      title: '工序任务编号，0表示非工序投影',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'workshopId',
      title: '车间编号，0表示未指定',
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
      field: 'matId',
      title: '生产物料编号',
      minWidth: 120,
    },
    {
      field: 'matCode',
      title: '物料编码快照',
      minWidth: 120,
    },
    {
      field: 'routeOpId',
      title: '路线工序编号，0表示需求或整单',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '当前资源编号，0表示未分配',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '计划数量或剩余数量',
      minWidth: 120,
    },
    {
      field: 'completedQty',
      title: '已完成数量快照',
      minWidth: 120,
    },
    {
      field: 'dueDate',
      title: '交期',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'priority',
      title: '优先级',
      minWidth: 120,
    },
    {
      field: 'currentStatus',
      title: '来源对象状态快照',
      minWidth: 120,
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
      field: 'lockedFlag',
      title: '是否已开工或业务锁定',
      minWidth: 120,
    },
    {
      field: 'sourceUpdateTime',
      title: '来源数据更新时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '投影快照时间',
      minWidth: 120,
      formatter: 'formatDateTime',
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
      field: 'scenarioCode',
      title: '排程场景编码',
      minWidth: 120,
    },
    {
      field: 'scenarioName',
      title: '排程场景名称',
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