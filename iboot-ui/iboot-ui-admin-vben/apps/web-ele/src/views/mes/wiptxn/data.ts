import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWipTxnApi } from '#/api/mes/wiptxn';

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
      fieldName: 'txnNo',
      label: '在制事务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入在制事务编号',
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
      label: '工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP',
      },
    },
    {
      fieldName: 'qty',
      label: '事务数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入事务数量',
      },
    },
    {
      fieldName: 'fromStatus',
      label: '变更前状态',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'toStatus',
      label: '变更后状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromOpTaskId',
      label: '来源工序任务编号，0表示无',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源工序任务编号，0表示无',
      },
    },
    {
      fieldName: 'toOpTaskId',
      label: '目标工序任务编号，0表示无',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标工序任务编号，0表示无',
      },
    },
    {
      fieldName: 'txnTime',
      label: '事务发生时间',
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
      fieldName: 'idempotencyKey',
      label: '事务幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入事务幂等键',
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
      label: 'MES-在制流转流水数据',
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
      fieldName: 'txnNo',
      label: '在制事务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入在制事务编号',
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
      label: '工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP',
      },
    },
    {
      fieldName: 'qty',
      label: '事务数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入事务数量',
      },
    },
    {
      fieldName: 'fromStatus',
      label: '变更前状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择变更前状态',
      },
    },
    {
      fieldName: 'toStatus',
      label: '变更后状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择变更后状态',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromOpTaskId',
      label: '来源工序任务编号，0表示无',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源工序任务编号，0表示无',
      },
    },
    {
      fieldName: 'toOpTaskId',
      label: '目标工序任务编号，0表示无',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标工序任务编号，0表示无',
      },
    },
    {
      fieldName: 'txnTime',
      label: '事务发生时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '事务幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入事务幂等键',
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
export function useGridColumns(): VxeTableGridOptions<MesWipTxnApi.WipTxn>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'txnNo',
      title: '在制事务编号',
      minWidth: 120,
    },
    {
      field: 'txnType',
      title: '事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '事务数量',
      minWidth: 120,
    },
    {
      field: 'fromStatus',
      title: '变更前状态',
      minWidth: 120,
    },
    {
      field: 'toStatus',
      title: '变更后状态',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '来源业务类型',
      minWidth: 120,
    },
    {
      field: 'bizNo',
      title: '来源业务单号',
      minWidth: 120,
    },
    {
      field: 'fromOpTaskId',
      title: '来源工序任务编号，0表示无',
      minWidth: 120,
    },
    {
      field: 'toOpTaskId',
      title: '目标工序任务编号，0表示无',
      minWidth: 120,
    },
    {
      field: 'txnTime',
      title: '事务发生时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'idempotencyKey',
      title: '事务幂等键',
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
      field: 'workOrderWork_order_no',
      title: '作业工单work_order_no',
      minWidth: 120,
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