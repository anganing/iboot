import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesRgvTaskApi } from '#/api/mes/rgvtask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { RES_RELATION_PROPS } from '#/views/mdm/relation-config';

import {
  OP_TASK_RELATION_PROPS,
  WORK_ORDER_RELATION_PROPS,
} from '../relation-config';

/** 工位选择器：工位落在 mdm 的生产资源上，复用资源选择器只换标题与提示。 */
const STATION_RELATION_PROPS = {
  ...RES_RELATION_PROPS,
  dialogTitle: '选择工位',
};

/**
 * 新增/修改的表单。
 *
 * 完成时间由外部调度系统回报；已下发的任务不允许再改搬运起终点，规则在后端校验。
 */
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
      label: '本地任务号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'taskType',
      label: '任务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_RGV_TASK_TYPE, 'string'),
        placeholder: '请选择任务类型',
      },
      defaultValue: 'STATION_TRANSFER',
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WORK_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'opTaskId',
      label: '来源工序任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'targetOpTaskId',
      label: '目标工序任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...OP_TASK_RELATION_PROPS,
        placeholder: '回库时留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'fromStationId',
      label: '来源工位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...STATION_RELATION_PROPS,
        placeholder: '请选择来源工位',
      },
    },
    {
      fieldName: 'toStationId',
      label: '目标工位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...STATION_RELATION_PROPS,
        placeholder: '不能与来源工位相同',
      },
    },
    {
      fieldName: 'carrierNo',
      label: '载具编号',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入载具编号',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部调度系统',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '如 WCS-A',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部任务号',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '同一外部系统下不能重复',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_RGV_DISPATCH_STATUS, 'string'),
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
        options: getDictOptions(DICT_TYPE.MES_RGV_TASK_STATUS, 'string'),
        placeholder: '请选择执行状态',
      },
      defaultValue: 'CREATED',
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '外部系统重复下发时用于去重',
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

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '本地任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入本地任务号',
      },
    },
    {
      fieldName: 'externalTaskNo',
      label: '外部任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部任务号',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      component: markRaw(RelationSelect),
      componentProps: WORK_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'opTaskId',
      label: '来源工序任务',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
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
      fieldName: 'taskType',
      label: '任务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_RGV_TASK_TYPE, 'string'),
        placeholder: '请选择任务类型',
      },
    },
    {
      fieldName: 'dispatchStatus',
      label: '下发状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_RGV_DISPATCH_STATUS, 'string'),
        placeholder: '请选择下发状态',
      },
    },
    {
      fieldName: 'status',
      label: '执行状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_RGV_TASK_STATUS, 'string'),
        placeholder: '请选择执行状态',
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
    { field: 'taskNo', title: '本地任务号', minWidth: 160 },
    { field: 'externalTaskNo', title: '外部任务号', minWidth: 160 },
    {
      field: 'taskType',
      title: '任务类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_RGV_TASK_TYPE },
      },
    },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '来源工序任务', minWidth: 160 },
    { field: 'targetOpTaskNo', title: '目标工序任务', minWidth: 160 },
    { field: 'fromStationName', title: '来源工位', minWidth: 140 },
    { field: 'toStationName', title: '目标工位', minWidth: 140 },
    { field: 'carrierNo', title: '载具编号', minWidth: 130 },
    { field: 'externalSystemCode', title: '外部调度系统', minWidth: 130 },
    {
      field: 'dispatchStatus',
      title: '下发状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_RGV_DISPATCH_STATUS },
      },
    },
    {
      field: 'status',
      title: '执行状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_RGV_TASK_STATUS },
      },
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
