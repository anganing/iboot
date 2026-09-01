import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcItemApi } from '#/api/mes/qcitem';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { INT_PROPS, QTY_PROPS } from '../form-props';
import { QC_PLAN_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 检验项是检验方案的子表，只有草稿状态的方案允许维护；数值型必须给上下限，规则在后端校验。
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
      fieldName: 'planId',
      label: '检验方案',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_PLAN_RELATION_PROPS,
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入检验项编码',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入检验项名称',
      },
    },
    {
      fieldName: 'itemType',
      label: '检验项类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_ITEM_TYPE, 'string'),
        placeholder: '请选择检验项类型',
      },
      defaultValue: 'NUMERIC',
    },
    {
      fieldName: 'unit',
      label: '检验值单位',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '如 mm、kg',
      },
    },
    {
      fieldName: 'stdValue',
      label: '标准值',
      component: 'Input',
      componentProps: {
        maxLength: 255,
        placeholder: '请输入标准值',
      },
    },
    {
      fieldName: 'lowerLimit',
      label: '数值下限',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入数值下限' },
      // 只有数值型检验项才需要上下限
      dependencies: {
        triggerFields: ['itemType'],
        show: (values) => values.itemType === 'NUMERIC',
      },
    },
    {
      fieldName: 'upperLimit',
      label: '数值上限',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入数值上限' },
      dependencies: {
        triggerFields: ['itemType'],
        show: (values) => values.itemType === 'NUMERIC',
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必检',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: true,
    },
    {
      fieldName: 'sortNo',
      label: '排序号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...INT_PROPS, placeholder: '数值越小越靠前' },
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
      fieldName: 'planId',
      label: '检验方案',
      component: markRaw(RelationSelect),
      componentProps: QC_PLAN_RELATION_PROPS,
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项编码',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项名称',
      },
    },
    {
      fieldName: 'itemType',
      label: '检验项类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_ITEM_TYPE, 'string'),
        placeholder: '请选择检验项类型',
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必检',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否必检',
      },
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
export function useGridColumns(): VxeTableGridOptions<MesQcItemApi.QcItem>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'planCode', title: '方案编码', minWidth: 150 },
    { field: 'planName', title: '方案名称', minWidth: 170 },
    { field: 'itemCode', title: '检验项编码', minWidth: 140 },
    { field: 'itemName', title: '检验项名称', minWidth: 170 },
    {
      field: 'itemType',
      title: '检验项类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_ITEM_TYPE },
      },
    },
    { field: 'unit', title: '单位', minWidth: 90 },
    { field: 'stdValue', title: '标准值', minWidth: 120 },
    { field: 'lowerLimit', title: '数值下限', minWidth: 110 },
    { field: 'upperLimit', title: '数值上限', minWidth: 110 },
    {
      field: 'requiredFlag',
      title: '是否必检',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'sortNo', title: '排序号', minWidth: 90 },
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
