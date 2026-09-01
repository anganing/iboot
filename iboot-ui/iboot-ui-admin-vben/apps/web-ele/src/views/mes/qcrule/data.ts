import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcRuleApi } from '#/api/mes/qcrule';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  MAT_CAT_RELATION_PROPS,
  MAT_RELATION_PROPS,
  OP_RELATION_PROPS,
  SUPPLIER_RELATION_PROPS,
  WC_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { INT_PROPS } from '../form-props';
import { QC_PLAN_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 物料、物料分类、工序、工作中心、供应商都是可选匹配维度，留空表示不限；
 * 引用的检验方案必须已发布，选择器已按状态过滤，后端会再校验一次。
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
      fieldName: 'code',
      label: '规则编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'name',
      label: '规则名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入规则名称',
      },
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
      defaultValue: 'IQC',
    },
    {
      fieldName: 'objectType',
      label: '检验对象类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_OBJECT_TYPE, 'string'),
        placeholder: '请选择检验对象类型',
      },
      defaultValue: 'MATERIAL',
    },
    {
      fieldName: 'qcPlanId',
      label: '检验方案',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_PLAN_RELATION_PROPS,
    },
    {
      fieldName: 'qcMode',
      label: '检验模式',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_MODE, 'string'),
        placeholder: '请选择检验模式',
      },
      defaultValue: 'PROCESS',
    },
    {
      fieldName: 'matId',
      label: '指定物料',
      component: markRaw(RelationSelect),
      componentProps: { ...MAT_RELATION_PROPS, placeholder: '不限留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'matCatId',
      label: '指定物料分类',
      component: markRaw(RelationSelect),
      componentProps: { ...MAT_CAT_RELATION_PROPS, placeholder: '不限留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'opId',
      label: '指定工序',
      component: markRaw(RelationSelect),
      componentProps: { ...OP_RELATION_PROPS, placeholder: '不限留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'wcId',
      label: '指定工作中心',
      component: markRaw(RelationSelect),
      componentProps: { ...WC_RELATION_PROPS, placeholder: '不限留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'supplierId',
      label: '指定供应商',
      component: markRaw(RelationSelect),
      componentProps: { ...SUPPLIER_RELATION_PROPS, placeholder: '不限留空' },
      defaultValue: 0,
    },
    {
      fieldName: 'priority',
      label: '优先级',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...INT_PROPS, placeholder: '数值越大越优先' },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
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
      fieldName: 'code',
      label: '规则编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入规则编码',
      },
    },
    {
      fieldName: 'name',
      label: '规则名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入规则名称',
      },
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
      fieldName: 'objectType',
      label: '检验对象类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_OBJECT_TYPE, 'string'),
        placeholder: '请选择检验对象类型',
      },
    },
    {
      fieldName: 'matId',
      label: '指定物料',
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
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<MesQcRuleApi.QcRule>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '规则编码', minWidth: 150 },
    { field: 'name', title: '规则名称', minWidth: 180 },
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
      field: 'objectType',
      title: '检验对象类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_OBJECT_TYPE },
      },
    },
    { field: 'matName', title: '指定物料', minWidth: 150 },
    { field: 'opName', title: '指定工序', minWidth: 140 },
    { field: 'wcName', title: '指定工作中心', minWidth: 140 },
    { field: 'qcPlanCode', title: '方案编码', minWidth: 150 },
    { field: 'qcPlanName', title: '方案名称', minWidth: 170 },
    {
      field: 'qcMode',
      title: '检验模式',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_QC_MODE } },
    },
    { field: 'priority', title: '优先级', minWidth: 90 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
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
