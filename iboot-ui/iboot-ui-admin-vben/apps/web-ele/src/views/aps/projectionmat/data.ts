import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionMatApi } from '#/api/aps/projectionmat';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS } from '#/views/mdm/relation-config';

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
      fieldName: 'sourceMatId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入物料编码' },
    },
    {
      fieldName: 'matName',
      label: '物料名称',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入物料名称' },
    },
    {
      fieldName: 'matType',
      label: '物料类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MDM_MAT_TYPE, 'string'),
        placeholder: '请选择物料类型',
      },
    },
    {
      fieldName: 'batchFlag',
      label: '是否批次管控',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<ApsProjectionMatApi.ProjectionMat>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 160 },
    {
      field: 'matType',
      title: '物料类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MDM_MAT_TYPE },
      },
    },
    { field: 'baseUomCode', title: '基准单位', minWidth: 110 },
    {
      field: 'batchFlag',
      title: '是否批次管控',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'bomVersion', title: 'BOM版本', minWidth: 110 },
    { field: 'routeVersion', title: '工艺路线版本', minWidth: 130 },
    {
      field: 'sourceUpdateTime',
      title: '来源更新时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '投影快照时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
  ];
}
