import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionInvApi } from '#/api/aps/projectioninv';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS, PROJECT_RELATION_PROPS } from '#/views/mdm/relation-config';
import { LOC_RELATION_PROPS, WH_RELATION_PROPS } from '#/views/wms/relation-config';

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
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入物料编码' },
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入批次号' },
    },
    {
      fieldName: 'projectId',
      label: '项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
    },
    {
      fieldName: 'stockScope',
      label: '库存范围',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_STOCK_SCOPE, 'string'),
        placeholder: '请选择库存范围',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择质量状态',
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

export function useGridColumns(): VxeTableGridOptions<ApsProjectionInvApi.ProjectionInv>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'plantName', title: '工厂', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 140 },
    { field: 'locName', title: '库位', minWidth: 140 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 120 },
    { field: 'projectName', title: '项目', minWidth: 140 },
    {
      field: 'stockScope',
      title: '库存范围',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_STOCK_SCOPE } },
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    { field: 'qtyOnhand', title: '现存数量', minWidth: 110 },
    { field: 'qtyReserved', title: '预留数量', minWidth: 110 },
    { field: 'qtyFrozen', title: '冻结数量', minWidth: 110 },
    { field: 'qtyAvailable', title: '可用数量', minWidth: 110 },
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
