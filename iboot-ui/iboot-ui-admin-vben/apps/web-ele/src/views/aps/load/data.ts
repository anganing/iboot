import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsLoadApi } from '#/api/aps/load';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { RES_RELATION_PROPS } from '#/views/mdm/relation-config';

import { APS_VERSION_RELATION_PROPS } from '../relation-config';

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'versionId',
      label: '排程版本',
      component: markRaw(RelationSelect),
      componentProps: APS_VERSION_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
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
      fieldName: 'overloadOnly',
      label: '只看超负荷',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择',
      },
    },
    {
      fieldName: 'workDate',
      label: '负荷日期',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<ApsLoadApi.Load>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    { field: 'resCode', title: '资源编码', minWidth: 140 },
    { field: 'resName', title: '资源名称', minWidth: 160 },
    { field: 'workDate', title: '负荷日期', minWidth: 120 },
    { field: 'loadMinutes', title: '计划负荷(分钟)', minWidth: 130 },
    { field: 'capacityMinutes', title: '可用产能(分钟)', minWidth: 130 },
    { field: 'loadRate', title: '负荷率', minWidth: 100 },
    { field: 'overloadMinutes', title: '超负荷(分钟)', minWidth: 130 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
  ];
}
