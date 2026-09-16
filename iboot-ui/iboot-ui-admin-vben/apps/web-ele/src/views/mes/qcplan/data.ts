import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcItemApi } from '#/api/mes/qcitem';
import type { MesQcPlanApi } from '#/api/mes/qcplan';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

import { INT_PROPS, QTY_PROPS } from '../form-props';

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
      // 固定「检验项目 / 发布 / 停用」3 位 + 文字「更多」1 位 = 4 位，取 280 档
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/**
 * 检验项目（方案子表）新增/修改表单。
 *
 * 父键 `planId` 由抽屉 `setData({ planId })` 传入并在提交时回填，不作为可见字段——
 * 用户已经在某个方案的抽屉里，再让他选一次方案只会把项目改到别的方案底下去。
 */
export function useQcItemFormSchema(): VbenFormSchema[] {
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

/** 检验项目子表的筛选表单：不含 planId，父标识由抽屉固定。 */
export function useQcItemGridFormSchema(): VbenFormSchema[] {
  return [
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
  ];
}

/**
 * 检验项目子表的列。
 *
 * 不列 `planCode` / `planName`：整张表都在当前方案的抽屉里，父标识重复两遍只会吃掉横向宽度。
 */
export function useQcItemGridColumns(): VxeTableGridOptions<MesQcItemApi.QcItem>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
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
      // 固定「修改 / 删除」2 位，无「更多」，取 160 档
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
