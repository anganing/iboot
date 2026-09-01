import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionOrderApi } from '#/api/aps/projectionorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS, PLANT_RELATION_PROPS, PROJECT_RELATION_PROPS, WORKSHOP_RELATION_PROPS } from '#/views/mdm/relation-config';
import { RES_RELATION_PROPS } from '#/views/mdm/relation-config';

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
      fieldName: 'sourceType',
      label: '来源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_PROJECTION_SOURCE_TYPE, 'string'),
        placeholder: '请选择来源类型',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入来源单号' },
    },
    {
      fieldName: 'matId',
      label: '生产物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
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
      fieldName: 'projectId',
      label: '项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '当前资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'currentStatus',
      label: '来源对象状态',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入状态' },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择',
      },
    },
    {
      fieldName: 'materialShortageFlag',
      label: '是否缺料',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择',
      },
    },
    {
      fieldName: 'dueDate',
      label: '交期',
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

export function useGridColumns(): VxeTableGridOptions<ApsProjectionOrderApi.ProjectionOrder>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    {
      field: 'sourceType',
      title: '来源类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.APS_PROJECTION_SOURCE_TYPE } },
    },
    { field: 'sourceNo', title: '来源单号', minWidth: 150 },
    { field: 'plantName', title: '工厂', minWidth: 140 },
    { field: 'workshopName', title: '车间', minWidth: 140 },
    { field: 'projectName', title: '项目', minWidth: 140 },
    { field: 'projectTaskName', title: '项目任务', minWidth: 140 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'resName', title: '当前资源', minWidth: 150 },
    { field: 'qty', title: '计划数量', minWidth: 110 },
    { field: 'completedQty', title: '已完成数量', minWidth: 120 },
    { field: 'dueDate', title: '交期', minWidth: 160, formatter: 'formatDateTime' },
    { field: 'priority', title: '优先级', minWidth: 90 },
    {
      field: 'currentStatus',
      title: '来源对象状态',
      minWidth: 140,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_OP_TASK_STATUS } },
    },
    {
      field: 'lockedFlag',
      title: '是否锁定',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING } },
    },
    {
      field: 'materialShortageFlag',
      title: '是否缺料',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING } },
    },
    { field: 'materialShortQty', title: '缺料数量', minWidth: 110 },
    { field: 'snapshotTime', title: '投影快照时间', minWidth: 160, formatter: 'formatDateTime' },
    { field: 'createTime', title: '创建时间', minWidth: 160, formatter: 'formatDateTime' },
  ];
}
