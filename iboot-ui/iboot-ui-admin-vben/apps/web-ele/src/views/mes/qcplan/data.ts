import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcPlanApi } from '#/api/mes/qcplan';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

import { QTY_PROPS } from '../form-props';

/**
 * 新增/修改的表单。
 *
 * 抽样值与 AQL 的必填性由抽样类型决定，跨字段规则在后端 Service 校验，前端按抽样类型控制显隐。
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
      label: '方案编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'name',
      label: '方案名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入方案名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '方案版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '如 V1.0',
      },
    },
    {
      fieldName: 'sampleType',
      label: '抽样类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_SAMPLE_TYPE, 'string'),
        placeholder: '请选择抽样类型',
      },
      defaultValue: 'FULL',
    },
    {
      fieldName: 'sampleValue',
      label: '抽样值',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '固定数量或抽样比例' },
      // 固定数量与按比例抽检才需要抽样值，全检与 AQL 抽检不需要
      dependencies: {
        triggerFields: ['sampleType'],
        show: (values) =>
          values.sampleType === 'FIXED_QTY' || values.sampleType === 'PERCENT',
      },
    },
    {
      fieldName: 'aql',
      label: 'AQL 质量限',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '如 1.0',
      },
      dependencies: {
        triggerFields: ['sampleType'],
        show: (values) => values.sampleType === 'AQL',
      },
    },
    {
      fieldName: 'acceptanceRule',
      label: '接收判定规则',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入接收判定规则',
        rows: 2,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_PLAN_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
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
      label: '方案编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入方案编码',
      },
    },
    {
      fieldName: 'name',
      label: '方案名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入方案名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入版本号',
      },
    },
    {
      fieldName: 'sampleType',
      label: '抽样类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_SAMPLE_TYPE, 'string'),
        placeholder: '请选择抽样类型',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_PLAN_STATUS, 'string'),
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
export function useGridColumns(): VxeTableGridOptions<MesQcPlanApi.QcPlan>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '方案编码', minWidth: 150 },
    { field: 'name', title: '方案名称', minWidth: 180 },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    {
      field: 'sampleType',
      title: '抽样类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_SAMPLE_TYPE },
      },
    },
    { field: 'sampleValue', title: '抽样值', minWidth: 110 },
    { field: 'aql', title: 'AQL', minWidth: 100 },
    { field: 'acceptanceRule', title: '接收判定规则', minWidth: 180 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_PLAN_STATUS },
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
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
