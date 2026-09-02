import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsScenarioApi } from '#/api/aps/scenario';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  PLANT_RELATION_PROPS,
  WORKSHOP_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS } from '../form-props';
import { APS_STRATEGY_RELATION_PROPS } from '../relation-config';

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
      fieldName: 'code',
      label: '场景编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则分配',
      },
    },
    {
      fieldName: 'name',
      label: '场景名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入场景名称',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'workshopId',
      label: '车间',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WORKSHOP_RELATION_PROPS,
        placeholder: '留空表示全工厂',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'strategyId',
      label: '排程策略',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: APS_STRATEGY_RELATION_PROPS,
    },
    {
      fieldName: 'planStartTime',
      label: '计划开始时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划开始时间' },
    },
    {
      fieldName: 'planEndTime',
      label: '计划结束时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划结束时间' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.APS_SCENARIO_STATUS, 'string'),
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
      fieldName: 'code',
      label: '场景编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入场景编码',
      },
    },
    {
      fieldName: 'name',
      label: '场景名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入场景名称',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'workshopId',
      label: '车间',
      component: markRaw(RelationSelect),
      componentProps: WORKSHOP_RELATION_PROPS,
    },
    {
      fieldName: 'strategyId',
      label: '排程策略',
      component: markRaw(RelationSelect),
      componentProps: APS_STRATEGY_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_SCENARIO_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<ApsScenarioApi.Scenario>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '场景编码', minWidth: 150 },
    { field: 'name', title: '场景名称', minWidth: 160 },
    { field: 'plantName', title: '工厂', minWidth: 130 },
    {
      field: 'workshopName',
      title: '车间',
      minWidth: 130,
      formatter: ({ row }) => row.workshopName || '全工厂',
    },
    { field: 'strategyCode', title: '策略编码', minWidth: 140 },
    { field: 'strategyName', title: '策略名称', minWidth: 150 },
    {
      field: 'planStartTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'planEndTime',
      title: '计划结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '输入快照时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_SCENARIO_STATUS },
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
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
