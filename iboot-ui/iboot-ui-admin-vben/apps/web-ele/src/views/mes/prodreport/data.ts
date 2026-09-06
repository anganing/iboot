import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdReportApi } from '#/api/mes/prodreport';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  RES_RELATION_PROPS,
  SHIFT_RELATION_PROPS,
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../form-props';
import { OP_TASK_RELATION_PROPS } from '../relation-config';

/**
 * 提交报工表单。创建即提交，界面上没有草稿态。
 *
 * 生产工单与作业工单编号由后端从工序任务反查带出，前端只提交工序任务，避免三者对不上。
 */
export function useSubmitFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'workerId',
      label: '报工人员',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: {
        ...RES_RELATION_PROPS,
        placeholder: '未指定可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: {
        ...SHIFT_RELATION_PROPS,
        placeholder: '不区分班次可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyGood',
      label: '合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入合格数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyScrap',
      label: '报废数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入报废数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyRework',
      label: '返工数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入返工数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'startTime',
      label: '作业开始时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择作业开始时间' },
    },
    {
      fieldName: 'endTime',
      label: '作业结束时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择作业结束时间' },
    },
    {
      fieldName: 'reportTime',
      label: '报工时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '留空取当前时间' },
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '扫码端提交时用于去重，必填',
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
      fieldName: 'reportNo',
      label: '报工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报工单号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'workerId',
      label: '报工人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_PROD_REPORT_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'reportTime',
      label: '报工时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesProdReportApi.ProdReport>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'reportNo', title: '报工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'workerName', title: '报工人员', minWidth: 120 },
    { field: 'resName', title: '生产资源', minWidth: 140 },
    { field: 'shiftName', title: '班次', minWidth: 110 },
    { field: 'qtyGood', title: '合格数量', minWidth: 110 },
    { field: 'qtyScrap', title: '报废数量', minWidth: 110 },
    { field: 'qtyRework', title: '返工数量', minWidth: 110 },
    {
      field: 'startTime',
      title: '作业开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '作业结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'reportTime',
      title: '报工时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_PROD_REPORT_STATUS },
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
