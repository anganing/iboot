import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionResourceApi } from '#/api/aps/projectionresource';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { PLANT_RELATION_PROPS, RES_RELATION_PROPS, WC_RELATION_PROPS, WORKSHOP_RELATION_PROPS } from '#/views/mdm/relation-config';
import { SHIFT_RELATION_PROPS } from '#/views/mdm/relation-config';

import { APS_SCENARIO_RELATION_PROPS } from '../relation-config';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'scenarioId',
      label: '排程场景',
      component: markRaw(RelationSelect),
      componentProps: APS_SCENARIO_RELATION_PROPS,
    },
    {
      fieldName: 'sourceResId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'resCode',
      label: '资源编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入资源编码' },
    },
    {
      fieldName: 'resName',
      label: '资源名称',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入资源名称' },
    },
    {
      fieldName: 'resType',
      label: '资源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MDM_RES_TYPE, 'string'),
        placeholder: '请选择资源类型',
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
      fieldName: 'wcId',
      label: '工作中心',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'workDate',
      label: '产能日期',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<ApsProjectionResourceApi.ProjectionResource>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'resCode', title: '资源编码', minWidth: 140 },
    { field: 'resName', title: '资源名称', minWidth: 160 },
    {
      field: 'resType',
      title: '资源类型',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MDM_RES_TYPE } },
    },
    { field: 'plantName', title: '工厂', minWidth: 140 },
    { field: 'workshopName', title: '车间', minWidth: 140 },
    { field: 'wcName', title: '工作中心', minWidth: 140 },
    { field: 'workDate', title: '产能日期', minWidth: 120 },
    { field: 'shiftName', title: '班次', minWidth: 130 },
    { field: 'availableMinutes', title: '可用产能(分钟)', minWidth: 140 },
    { field: 'sourceUpdateTime', title: '来源更新时间', minWidth: 160, formatter: 'formatDateTime' },
    { field: 'snapshotTime', title: '投影快照时间', minWidth: 160, formatter: 'formatDateTime' },
    { field: 'createTime', title: '创建时间', minWidth: 160, formatter: 'formatDateTime' },
  ];
}
