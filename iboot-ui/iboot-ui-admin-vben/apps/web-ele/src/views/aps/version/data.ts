import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import {
  APS_SCENARIO_RELATION_PROPS,
  APS_STRATEGY_RELATION_PROPS,
} from '../relation-config';

/**
 * 得分格式化。
 *
 * 求解得分动辄上亿（软分按分钟累加），不加千分位根本数不清位数；未求解完的版本得分为空，
 * 显示"-"而不是 0，两者含义完全不同。
 */
function formatScore(value: unknown) {
  return typeof value === 'number' ? value.toLocaleString('zh-CN') : '-';
}

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
    {
      field: 'hardScore',
      title: '硬约束得分',
      minWidth: 120,
      titlePrefix: {
        content:
          '硬约束违反的罚分总和。0 表示计划可行；小于 0 表示存在无法回避的冲突，版本判为求解失败，不允许发布',
      },
      formatter: ({ cellValue }) => formatScore(cellValue),
    },
    {
      field: 'softScore',
      title: '软约束得分',
      minWidth: 120,
      titlePrefix: {
        content:
          '各软约束按权重加权后的罚分总和，越接近 0 越好。它的绝对值跟任务数量、策略权重都有关，只在同策略同场景的版本之间比较才有意义',
      },
      formatter: ({ cellValue }) => formatScore(cellValue),
    },
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
      // 五个动作：进度、甘特图、约束命中、确认、发布
      width: 400,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 生成排程版本的表单 */
export function useGenerateFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'scenarioId',
      label: '排程场景',
      component: markRaw(RelationSelect),
      componentProps: APS_SCENARIO_RELATION_PROPS,
      rules: 'selectRequired',
    },
    {
      fieldName: 'strategyId',
      label: '排程策略',
      component: markRaw(RelationSelect),
      componentProps: APS_STRATEGY_RELATION_PROPS,
      // 留空沿用场景上引用的策略，避免每次生成都要重复选一次
      help: '留空则使用场景上引用的策略',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxlength: 500,
        rows: 3,
        showWordLimit: true,
        placeholder: '请输入备注',
      },
    },
  ];
}
