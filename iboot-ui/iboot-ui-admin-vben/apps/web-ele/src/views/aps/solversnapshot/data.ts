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
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/**
 * 约束命中对照表的列。
 *
 * 这张表回答"为什么是这个分数"：每条约束本次生效的权重、命中了多少次、扣掉多少分。
 * 权重列与策略配置页的权重编辑器是同一个量纲（都取正数），计划员才能对着调。
 * 命中次数与扣分允许点表头排序，便于先看谁在拖后腿。
 */
export function useConstraintMatchColumns(): VxeTableGridOptions<ApsSolverSnapshotApi.ConstraintMatchItem>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'displayName', title: '约束', minWidth: 150, slots: { default: 'constraint' } },
    { field: 'categoryName', title: '类别', width: 90 },
    {
      field: 'hardWeight',
      title: '硬权重',
      width: 100,
      titlePrefix: { content: '本次求解实际生效的硬约束权重。非零即生效，数值大小不影响结果' },
      formatter: ({ cellValue }) => formatNumber(cellValue),
    },
    {
      field: 'softWeight',
      title: '软权重',
      width: 100,
      titlePrefix: { content: '本次求解实际生效的软约束权重。软约束之间按权重大小相互权衡' },
      formatter: ({ cellValue }) => formatNumber(cellValue),
    },
    {
      field: 'matchCount',
      title: '命中次数',
      width: 110,
      sortable: true,
      titlePrefix: { content: '该约束被违反的次数。0 次表示这条约束在本次排程里完全满足' },
      formatter: ({ cellValue }) => formatNumber(cellValue),
    },
    {
      field: 'hardScore',
      title: '硬分扣分',
      width: 120,
      sortable: true,
      slots: { default: 'hardScore' },
    },
    {
      field: 'softScore',
      title: '软分扣分',
      width: 130,
      sortable: true,
      titlePrefix: { content: '命中次数乘权重后的扣分。绝对值最大的那条就是当前计划的主要代价' },
      slots: { default: 'softScore' },
    },
  ];
}

/** 数值格式化：得分动辄上亿，不加千分位数不清位数；空值显示"-"而不是 0，两者含义不同。 */
export function formatNumber(value: unknown) {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '-';
}
