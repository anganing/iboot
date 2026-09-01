import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  MAT_RELATION_PROPS,
  UOM_RELATION_PROPS,
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';
import { BATCH_RELATION_PROPS } from '#/views/wms/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../form-props';
import {
  QC_PLAN_RELATION_PROPS,
  QC_RULE_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 方案版本、物料与单位快照、批次号由后端补全；合格与不合格数量由检验结果汇总，都不在表单里。
 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'taskNo',
      label: '质检任务号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'qcPlanId',
      label: '检验方案',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_PLAN_RELATION_PROPS,
    },
    {
      fieldName: 'ruleId',
      label: '命中质检规则',
      component: markRaw(RelationSelect),
      componentProps: {
        ...QC_RULE_RELATION_PROPS,
        placeholder: '手工建单可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_TRIGGER_POINT, 'string'),
        placeholder: '请选择触发点',
      },
      defaultValue: 'IPQC',
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_BIZ_TYPE, 'string'),
        placeholder: '请选择来源业务类型',
      },
      defaultValue: 'OP_TASK',
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'matId',
      label: '检验物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'batchId',
      label: '检验批次',
      component: markRaw(RelationSelect),
      componentProps: {
        ...BATCH_RELATION_PROPS,
        placeholder: '不区分批次可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'qty',
      label: '送检数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入送检数量' },
    },
    {
      fieldName: 'sampleQty',
      label: '抽样数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '不能大于送检数量' },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'assignedInspectorId',
      label: '指派检验员',
      component: markRaw(RelationSelect),
      componentProps: { ...USER_RELATION_PROPS, placeholder: '未指派可留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'inspectStartTime',
      label: '检验开始时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择检验开始时间' },
    },
    {
      fieldName: 'inspectEndTime',
      label: '检验结束时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择检验结束时间' },
    },
    {
      fieldName: 'conclusion',
      label: '检验结论',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_CONCLUSION, 'string'),
        placeholder: '完成时必填',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'CREATED',
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '业务自动触发时用于去重，手工建单可留空',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入备注',
        rows: 2,
      },
    },
  ];
}

/** 列表的搜索表单 */
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
      fieldName: 'qcPlanId',
      label: '检验方案',
      component: markRaw(RelationSelect),
      componentProps: QC_PLAN_RELATION_PROPS,
    },
    {
      fieldName: 'assignedInspectorId',
      label: '指派检验员',
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
      fieldName: 'conclusion',
      label: '检验结论',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_CONCLUSION, 'string'),
        placeholder: '请选择检验结论',
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

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 160 },
    {
      field: 'triggerPoint',
      title: '触发点',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_TRIGGER_POINT },
      },
    },
    {
      field: 'bizType',
      title: '来源业务类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_BIZ_TYPE } },
    },
    { field: 'bizNo', title: '来源业务单号', minWidth: 150 },
    { field: 'qcPlanCode', title: '方案编码', minWidth: 150 },
    { field: 'qcPlanVersion', title: '方案版本', minWidth: 110 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 130 },
    { field: 'qty', title: '送检数量', minWidth: 110 },
    { field: 'sampleQty', title: '抽样数量', minWidth: 110 },
    { field: 'uomCode', title: '单位', minWidth: 90 },
    { field: 'qtyQualified', title: '合格数量', minWidth: 110 },
    { field: 'qtyRejected', title: '不合格数量', minWidth: 110 },
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
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
