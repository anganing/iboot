import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import { APS_SCENARIO_RELATION_PROPS } from '../relation-config';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'scenarioId',
      label: '排程场景',
      component: markRaw(RelationSelect),
      componentProps: APS_SCENARIO_RELATION_PROPS,
    },
    {
      fieldName: 'versionNo',
      label: '版本号',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_VERSION_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'publishedBy',
      label: '发布人',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'publishedTime',
      label: '发布时间',
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
export function useGridColumns(): VxeTableGridOptions<ApsVersionApi.Version>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 150 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_VERSION_STATUS },
      },
    },
    { field: 'score', title: '综合得分', minWidth: 140 },
    { field: 'hardScore', title: '硬约束得分', minWidth: 120 },
    { field: 'softScore', title: '软约束得分', minWidth: 120 },
    {
      field: 'solveStartTime',
      title: '求解开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'solveEndTime',
      title: '求解结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'publishedTime',
      title: '发布时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'publishedByName', title: '发布人', minWidth: 120 },
    { field: 'solverMessage', title: '求解信息', minWidth: 200 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
