import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesRgvTaskApi } from '#/api/mes/rgvtask';

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
      label: 'RGV本地任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入RGV本地任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: 'RGV外部任务号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入RGV外部任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN',
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
      label: '来源工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源工序任务编号',
      },
    },
    {
      fieldName: 'targetOpTaskId',
      label: '目标工序任务编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标工序任务编号',
      },
    },
    {
      fieldName: 'fromStationId',
      label: '来源工位或资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源工位或资源编号',
      },
    },
    {
      fieldName: 'toStationId',
      label: '目标工位或资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标工位或资源编号',
      },
    },
    {
      fieldName: 'carrierNo',
      label: '载具编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入载具编号',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部调度系统编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部调度系统编码',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态：PENDING、SENT、FAILED',
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
      label: '执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '业务幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入业务幂等键',
      },
    },
    {
      fieldName: 'completedTime',
      label: '完成时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
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
      label: 'MES-RGV工位流转任务数据',
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
      label: 'RGV本地任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入RGV本地任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: 'RGV外部任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入RGV外部任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN',
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
      label: '来源工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源工序任务编号',
      },
    },
    {
      fieldName: 'targetOpTaskId',
      label: '目标工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标工序任务编号',
      },
    },
    {
      fieldName: 'fromStationId',
      label: '来源工位或资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源工位或资源编号',
      },
    },
    {
      fieldName: 'toStationId',
      label: '目标工位或资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标工位或资源编号',
      },
    },
    {
      fieldName: 'carrierNo',
      label: '载具编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入载具编号',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部调度系统编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部调度系统编码',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态：PENDING、SENT、FAILED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择下发状态：PENDING、SENT、FAILED',
      },
    },
    {
      fieldName: 'status',
      label: '执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '业务幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入业务幂等键',
      },
    },
    {
      fieldName: 'completedTime',
      label: '完成时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<MesRgvTaskApi.RgvTask>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskNo',
      title: 'RGV本地任务号',
      minWidth: 120,
    },
    {
      field: 'externalTaskNo',
      title: 'RGV外部任务号',
      minWidth: 120,
    },
    {
      field: 'taskType',
      title: '任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN',
      minWidth: 120,
    },
    {
      field: 'targetOpTaskId',
      title: '目标工序任务编号',
      minWidth: 120,
    },
    {
      field: 'fromStationId',
      title: '来源工位或资源编号',
      minWidth: 120,
    },
    {
      field: 'toStationId',
      title: '目标工位或资源编号',
      minWidth: 120,
    },
    {
      field: 'carrierNo',
      title: '载具编号',
      minWidth: 120,
    },
    {
      field: 'externalSystemCode',
      title: '外部调度系统编码',
      minWidth: 120,
    },
    {
      field: 'dispatchStatus',
      title: '下发状态：PENDING、SENT、FAILED',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '业务幂等键',
      minWidth: 120,
    },
    {
      field: 'completedTime',
      title: '完成时间',
      minWidth: 120,
      formatter: 'formatDateTime',
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
      field: 'opTaskTask_no',
      title: '来源工序任务task_no',
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