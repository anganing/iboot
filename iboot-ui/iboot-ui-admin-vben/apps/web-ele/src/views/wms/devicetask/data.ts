import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsDeviceTaskApi } from '#/api/wms/devicetask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import {
  CONTAINER_RELATION_PROPS,
  LOC_RELATION_PROPS,
} from '../relation-config';

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
      label: '任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入本地设备任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部任务号',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '设备系统回传的任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TASK_TYPE, 'string'),
        placeholder: '请选择任务类型',
      },
    },
    {
      fieldName: 'deviceType',
      label: '设备类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TYPE, 'string'),
        placeholder: '请选择设备类型',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部系统编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入外部设备系统编码',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'containerId',
      label: '搬运容器',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CONTAINER_RELATION_PROPS,
        placeholder: '无容器可留空',
      },
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'toLocId',
      label: '目标库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'actualLocId',
      label: '实际库位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        placeholder: '设备回传后填写',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_DISPATCH_STATUS, 'string'),
        placeholder: '请选择下发状态',
      },
      defaultValue: 'PENDING',
    },
    {
      fieldName: 'status',
      label: '执行状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TASK_STATUS, 'string'),
        placeholder: '请选择执行状态',
      },
      defaultValue: 'CREATED',
    },
    {
      fieldName: 'postStatus',
      label: '过账状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_POST_STATUS, 'string'),
        placeholder: '请选择库存过账状态',
      },
      defaultValue: 'PENDING',
    },
    {
      fieldName: 'completedTime',
      label: '完成时间',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择设备完成时间',
        showTime: true,
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入业务幂等键',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入备注',
        rows: 2,
      },
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '设备任务数据',
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
      label: '任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入本地设备任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部设备任务号',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TASK_TYPE, 'string'),
        placeholder: '请选择任务类型',
      },
    },
    {
      fieldName: 'deviceType',
      label: '设备类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TYPE, 'string'),
        placeholder: '请选择设备类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_DISPATCH_STATUS, 'string'),
        placeholder: '请选择下发状态',
      },
    },
    {
      fieldName: 'status',
      label: '执行状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_DEVICE_TASK_STATUS, 'string'),
        placeholder: '请选择执行状态',
      },
    },
    {
      fieldName: 'postStatus',
      label: '过账状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_POST_STATUS, 'string'),
        placeholder: '请选择库存过账状态',
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
    { field: 'taskNo', title: '任务号', minWidth: 160 },
    { field: 'externalTaskNo', title: '外部任务号', minWidth: 150 },
    {
      field: 'taskType',
      title: '任务类型',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_DEVICE_TASK_TYPE } },
    },
    {
      field: 'deviceType',
      title: '设备类型',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_DEVICE_TYPE } },
    },
    { field: 'sourceBizNo', title: '来源单号', minWidth: 150 },
    { field: 'containerNo', title: '搬运容器', minWidth: 140 },
    { field: 'fromLocCode', title: '来源库位', minWidth: 130 },
    { field: 'toLocCode', title: '目标库位', minWidth: 130 },
    { field: 'actualLocCode', title: '实际库位', minWidth: 130 },
    {
      field: 'dispatchStatus',
      title: '下发状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_DISPATCH_STATUS } },
    },
    {
      field: 'status',
      title: '执行状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_DEVICE_TASK_STATUS },
      },
    },
    {
      field: 'postStatus',
      title: '过账状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_POST_STATUS } },
    },
    {
      field: 'completedTime',
      title: '完成时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
