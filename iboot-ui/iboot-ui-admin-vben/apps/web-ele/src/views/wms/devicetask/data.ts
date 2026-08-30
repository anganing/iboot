import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsDeviceTaskApi } from '#/api/wms/devicetask';

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
      label: '本地设备任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入本地设备任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部设备任务号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部设备任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN',
      },
    },
    {
      fieldName: 'deviceType',
      label: '设备类型：WCS、AGV、RGV',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择设备类型：WCS、AGV、RGV',
      },
    },
    {
      fieldName: 'sourceBizType',
      label: '来源业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源业务单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'sourceLineId',
      label: '来源业务明细编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务明细编号',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示无容器',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器编号，0表示无容器',
      },
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源库位编号',
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标库位编号',
      },
    },
    {
      fieldName: 'actualLocId',
      label: '设备回传实际库位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入设备回传实际库位编号',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部设备系统编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部设备系统编码',
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
      fieldName: 'postStatus',
      label: '库存过账状态：PENDING、POSTED、FAILED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'requestSnapshotJson',
      label: '下发业务快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入下发业务快照',
      },
    },
    {
      fieldName: 'callbackSnapshotJson',
      label: '完成回调快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入完成回调快照',
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
      label: '设备完成时间',
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
      label: 'WMS-设备搬运任务数据',
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
      label: '本地设备任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入本地设备任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部设备任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部设备任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN',
      },
    },
    {
      fieldName: 'deviceType',
      label: '设备类型：WCS、AGV、RGV',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择设备类型：WCS、AGV、RGV',
      },
    },
    {
      fieldName: 'sourceBizType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'sourceLineId',
      label: '来源业务明细编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务明细编号',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示无容器',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器编号，0表示无容器',
      },
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源库位编号',
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标库位编号',
      },
    },
    {
      fieldName: 'actualLocId',
      label: '设备回传实际库位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入设备回传实际库位编号',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部设备系统编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部设备系统编码',
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
      fieldName: 'postStatus',
      label: '库存过账状态：PENDING、POSTED、FAILED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择库存过账状态：PENDING、POSTED、FAILED',
      },
    },
    {
      fieldName: 'requestSnapshotJson',
      label: '下发业务快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入下发业务快照',
      },
    },
    {
      fieldName: 'callbackSnapshotJson',
      label: '完成回调快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入完成回调快照',
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
      label: '设备完成时间',
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
export function useGridColumns(): VxeTableGridOptions<WmsDeviceTaskApi.DeviceTask>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskNo',
      title: '本地设备任务号',
      minWidth: 120,
    },
    {
      field: 'externalTaskNo',
      title: '外部设备任务号',
      minWidth: 120,
    },
    {
      field: 'taskType',
      title: '任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN',
      minWidth: 120,
    },
    {
      field: 'deviceType',
      title: '设备类型：WCS、AGV、RGV',
      minWidth: 120,
    },
    {
      field: 'sourceBizType',
      title: '来源业务类型',
      minWidth: 120,
    },
    {
      field: 'sourceBizNo',
      title: '来源业务单号',
      minWidth: 120,
    },
    {
      field: 'sourceLineId',
      title: '来源业务明细编号',
      minWidth: 120,
    },
    {
      field: 'containerId',
      title: '容器编号，0表示无容器',
      minWidth: 120,
    },
    {
      field: 'fromLocId',
      title: '来源库位编号',
      minWidth: 120,
    },
    {
      field: 'toLocId',
      title: '目标库位编号',
      minWidth: 120,
    },
    {
      field: 'actualLocId',
      title: '设备回传实际库位编号',
      minWidth: 120,
    },
    {
      field: 'externalSystemCode',
      title: '外部设备系统编码',
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
      field: 'postStatus',
      title: '库存过账状态：PENDING、POSTED、FAILED',
      minWidth: 120,
    },
    {
      field: 'requestSnapshotJson',
      title: '下发业务快照',
      minWidth: 120,
    },
    {
      field: 'callbackSnapshotJson',
      title: '完成回调快照',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '业务幂等键',
      minWidth: 120,
    },
    {
      field: 'completedTime',
      title: '设备完成时间',
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
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}