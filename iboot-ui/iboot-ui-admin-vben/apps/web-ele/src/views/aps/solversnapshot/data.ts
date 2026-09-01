import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'snapshotType',
      label: '快照类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_SNAPSHOT_TYPE, 'string'),
        placeholder: '请选择快照类型',
      },
    },
    {
      fieldName: 'checksum',
      label: '校验值',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入校验值' },
    },
    {
      fieldName: 'snapshotTime',
      label: '快照生成时间',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<ApsSolverSnapshotApi.SolverSnapshot>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'scenarioName', title: '场景名称', minWidth: 160 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    {
      field: 'snapshotType',
      title: '快照类型',
      minWidth: 140,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_SNAPSHOT_TYPE },
      },
    },
    { field: 'checksum', title: '校验值', minWidth: 220 },
    {
      field: 'snapshotTime',
      title: '快照生成时间',
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
