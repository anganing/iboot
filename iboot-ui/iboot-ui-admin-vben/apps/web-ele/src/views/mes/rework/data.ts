import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesReworkApi } from '#/api/mes/rework';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { QTY_PROPS } from '../form-props';
import {
  OP_TASK_RELATION_PROPS,
  QC_TASK_RELATION_PROPS,
  ROUTE_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 生产工单、作业工单编号与返工路线版本由后端带出，不出现在表单里。
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
      fieldName: 'reworkNo',
      label: '返工任务编号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '返工起始工序',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'sourceQcTaskId',
      label: '来源质检任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...QC_TASK_RELATION_PROPS,
        placeholder: '手工发起可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'qty',
      label: '返工数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入返工数量' },
    },
    {
      fieldName: 'routeId',
      label: '返工工艺路线',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: ROUTE_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_REWORK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
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
      fieldName: 'reworkNo',
      label: '返工任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工任务编号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '返工起始工序',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'sourceQcTaskId',
      label: '来源质检任务',
      component: markRaw(RelationSelect),
      componentProps: QC_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_REWORK_STATUS, 'string'),
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<MesReworkApi.Rework>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'reworkNo', title: '返工任务编号', minWidth: 160 },
    { field: 'opTaskNo', title: '返工起始工序', minWidth: 160 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'sourceQcTaskNo', title: '来源质检任务', minWidth: 160 },
    { field: 'qty', title: '返工数量', minWidth: 110 },
    { field: 'routeVersion', title: '返工路线版本', minWidth: 130 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_REWORK_STATUS },
      },
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
