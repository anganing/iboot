
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS, USER_RELATION_PROPS } from '#/views/mdm/relation-config';

/** 质检任务只能由业务规则生成，本页没有手工 CRUD 表单。 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '质检任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检任务号',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'matId',
      label: '检验物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'assignedInspectorId',
      label: '检验员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_TRIGGER_POINT, 'string'),
        placeholder: '请选择触发点',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'inspectStartTime',
      label: '检验开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    // 20 列宽表放不下规则快照、幂等键等字段，任务号是本行唯一的业务编号，用它做双击详情入口
    { field: 'taskNo', title: '质检任务号', minWidth: 160, ...codeLinkColumn() },
    {
      field: 'triggerPoint',
      title: '触发点',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TRIGGER_POINT },
      },
    },
    { field: 'bizNo', title: '来源业务单号', minWidth: 150 },
    { field: 'qcPlanCode', title: '方案编码', minWidth: 150 },
    { field: 'qcPlanVersion', title: '方案版本', minWidth: 110 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 130 },
    { field: 'qty', title: '送检数量', minWidth: 110 },
    { field: 'sampleQty', title: '抽样数量', minWidth: 110 },
    { field: 'assignedInspectorName', title: '检验员', minWidth: 120 },
    {
      field: 'conclusion',
      title: '检验结论',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_CONCLUSION },
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TASK_STATUS },
      },
    },
    {
      field: 'inspectStartTime',
      title: '检验开始时间',
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
      // 固定「指派 / 开始检验 / 提交结论」3 位 + 文字「更多」1 位 = 4 位，取 280 档
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/**
 * 作业页（检验录入 / 不合格处置）待办列表筛选条件。
 *
 * 不含 status 与 conclusion：作业范围由页面在 proxyConfig 里固定（待检只看 CREATED，
 * 处置只看 REJECTED），放进表单会让用户一改就把作业页退化成全量查询。
 */
export function useWorkbenchGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '质检任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检任务号',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'matId',
      label: '检验物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_TRIGGER_POINT, 'string'),
        placeholder: '请选择触发点',
      },
    },
    {
      fieldName: 'assignedInspectorId',
      label: '检验员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
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

/** 检验录入作业页左侧待检任务列，与单据列表共用同一后端模型。 */
export function useInspectFormColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 150 },
    { field: 'matCode', title: '物料编码', minWidth: 120 },
    { field: 'matName', title: '物料名称', minWidth: 140 },
    { field: 'sampleQty', title: '抽样数量', width: 100 },
    {
      field: 'status',
      title: '状态',
      width: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TASK_STATUS },
      },
    },
  ];
}
