import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispatchTaskApi } from '#/api/mes/dispatchtask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  RES_RELATION_PROPS,
  SHIFT_RELATION_PROPS,
  TEAM_RELATION_PROPS,
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS } from '../form-props';
import { OP_TASK_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 作业人员与作业班组至少填一个：派给个人还是派给班组由现场决定，两个都空这条派工没有接收方，
 * 后端会拦下。
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
      fieldName: 'dispatchNo',
      label: '派工单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'operatorId',
      label: '作业人员',
      component: markRaw(RelationSelect),
      componentProps: {
        ...USER_RELATION_PROPS,
        placeholder: '与作业班组至少填一个',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'teamId',
      label: '作业班组',
      component: markRaw(RelationSelect),
      componentProps: {
        ...TEAM_RELATION_PROPS,
        placeholder: '与作业人员至少填一个',
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
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划开始时间' },
    },
    {
      fieldName: 'plannedEndTime',
      label: '计划结束时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '不能早于开始时间' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_DISPATCH_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'PENDING',
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
      fieldName: 'dispatchNo',
      label: '派工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入派工单号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'operatorId',
      label: '作业人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'teamId',
      label: '作业班组',
      component: markRaw(RelationSelect),
      componentProps: TEAM_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DISPATCH_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
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
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesDispatchTaskApi.DispatchTask>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'dispatchNo', title: '派工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'operatorName', title: '作业人员', minWidth: 120 },
    { field: 'teamName', title: '作业班组', minWidth: 130 },
    { field: 'resName', title: '生产资源', minWidth: 140 },
    { field: 'shiftName', title: '班次', minWidth: 110 },
    {
      field: 'plannedStartTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'plannedEndTime',
      title: '计划结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DISPATCH_TASK_STATUS },
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
