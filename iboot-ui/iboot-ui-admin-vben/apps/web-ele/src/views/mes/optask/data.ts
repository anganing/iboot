import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

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
      fieldName: 'taskNo',
      label: '工序任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务号',
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
      fieldName: 'workOrderNo',
      label: '作业工单号快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单号快照',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '锁定的路线工序编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的路线工序编号',
      },
    },
    {
      fieldName: 'opId',
      label: '标准工序编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标准工序编号',
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
      fieldName: 'seq',
      label: '工序顺序号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序顺序号',
      },
    },
    {
      fieldName: 'parallelGroup',
      label: '并行工序组',
      component: 'Input',
      componentProps: {
        placeholder: '请输入并行工序组',
      },
    },
    {
      fieldName: 'prevTaskId',
      label: '前序工序任务编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入前序工序任务编号',
      },
    },
    {
      fieldName: 'nextTaskId',
      label: '后序工序任务编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入后序工序任务编号',
      },
    },
    {
      fieldName: 'execMode',
      label: '执行模式：SEQUENTIAL、PARALLEL、OPTIONAL',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入执行模式：SEQUENTIAL、PARALLEL、OPTIONAL',
      },
    },
    {
      fieldName: 'qcMode',
      label: '质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      },
    },
    {
      fieldName: 'skipPolicy',
      label: '跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED',
      },
    },
    {
      fieldName: 'keyFlag',
      label: '是否关键工序',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
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
      label: '合格完成数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合格完成数量',
      },
    },
    {
      fieldName: 'scrapQty',
      label: '报废数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入报废数量',
      },
    },
    {
      fieldName: 'resId',
      label: '当前分配资源编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前分配资源编号',
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
      fieldName: 'status',
      label: '状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED',
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
      label: 'MES-工序任务数据',
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
      fieldName: 'taskNo',
      label: '工序任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务号',
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
      fieldName: 'workOrderNo',
      label: '作业工单号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单号快照',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '锁定的路线工序编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的路线工序编号',
      },
    },
    {
      fieldName: 'opId',
      label: '标准工序编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入标准工序编号',
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
      fieldName: 'seq',
      label: '工序顺序号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序顺序号',
      },
    },
    {
      fieldName: 'parallelGroup',
      label: '并行工序组',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入并行工序组',
      },
    },
    {
      fieldName: 'prevTaskId',
      label: '前序工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入前序工序任务编号',
      },
    },
    {
      fieldName: 'nextTaskId',
      label: '后序工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入后序工序任务编号',
      },
    },
    {
      fieldName: 'execMode',
      label: '执行模式：SEQUENTIAL、PARALLEL、OPTIONAL',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入执行模式：SEQUENTIAL、PARALLEL、OPTIONAL',
      },
    },
    {
      fieldName: 'qcMode',
      label: '质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      },
    },
    {
      fieldName: 'skipPolicy',
      label: '跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED',
      },
    },
    {
      fieldName: 'keyFlag',
      label: '是否关键工序',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否关键工序',
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
      label: '合格完成数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入合格完成数量',
      },
    },
    {
      fieldName: 'scrapQty',
      label: '报废数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报废数量',
      },
    },
    {
      fieldName: 'resId',
      label: '当前分配资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前分配资源编号',
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
      fieldName: 'status',
      label: '状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<MesOpTaskApi.OpTask>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskNo',
      title: '工序任务号',
      minWidth: 120,
    },
    {
      field: 'workOrderNo',
      title: '作业工单号快照',
      minWidth: 120,
    },
    {
      field: 'routeOpId',
      title: '锁定的路线工序编号',
      minWidth: 120,
    },
    {
      field: 'opId',
      title: '标准工序编号',
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
      field: 'seq',
      title: '工序顺序号',
      minWidth: 120,
    },
    {
      field: 'parallelGroup',
      title: '并行工序组',
      minWidth: 120,
    },
    {
      field: 'prevTaskId',
      title: '前序工序任务编号',
      minWidth: 120,
    },
    {
      field: 'nextTaskId',
      title: '后序工序任务编号',
      minWidth: 120,
    },
    {
      field: 'execMode',
      title: '执行模式：SEQUENTIAL、PARALLEL、OPTIONAL',
      minWidth: 120,
    },
    {
      field: 'qcMode',
      title: '质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE',
      minWidth: 120,
    },
    {
      field: 'skipPolicy',
      title: '跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED',
      minWidth: 120,
    },
    {
      field: 'keyFlag',
      title: '是否关键工序',
      minWidth: 120,
    },
    {
      field: 'plannedQty',
      title: '计划数量',
      minWidth: 120,
    },
    {
      field: 'completedQty',
      title: '合格完成数量',
      minWidth: 120,
    },
    {
      field: 'scrapQty',
      title: '报废数量',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '当前分配资源编号',
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
      field: 'status',
      title: '状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED',
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
      field: 'workOrderWork_order_no',
      title: '作业工单work_order_no',
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