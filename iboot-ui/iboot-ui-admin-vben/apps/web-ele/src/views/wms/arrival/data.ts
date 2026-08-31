import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PLANT_RELATION_PROPS,
  SUPPLIER_RELATION_PROPS,
  UOM_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { WH_RELATION_PROPS } from '../relation-config';

/** 数量控件的公共配置 */
const QTY_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  min: 0,
  precision: 6,
};

/** 新增/修改的表单 */
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
      fieldName: 'arrivalNo',
      label: '到货单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入到货登记单号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_SOURCE_TYPE, 'string'),
        placeholder: '请选择来源类型',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源单号',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '所属工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '目标仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'expectDate',
      label: '预计到货',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        placeholder: '请选择预计到货日期',
        type: 'date',
        valueFormat: 'YYYY-MM-DD',
      },
    },
    {
      fieldName: 'arrivalTime',
      label: '实际到货',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择实际到货时间',
        showTime: true,
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'status',
      label: '到货状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_STATUS, 'string'),
        placeholder: '请选择到货状态',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '到货登记数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'arrivalNo',
      label: '到货单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入到货登记单号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_SOURCE_TYPE, 'string'),
        placeholder: '请选择来源类型',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源单号',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '目标仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '到货状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_STATUS, 'string'),
        placeholder: '请选择到货状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsArrivalApi.Arrival>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'arrivalNo', title: '到货单号', minWidth: 160 },
    {
      field: 'sourceType',
      title: '来源类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_ARRIVAL_SOURCE_TYPE },
      },
    },
    { field: 'sourceNo', title: '来源单号', minWidth: 150 },
    { field: 'supplierCode', title: '供应商编码', minWidth: 130 },
    { field: 'supplierName', title: '供应商名称', minWidth: 160 },
    { field: 'whName', title: '目标仓库', minWidth: 140 },
    { field: 'expectDate', title: '预计到货', minWidth: 120 },
    {
      field: 'arrivalTime',
      title: '实际到货',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '到货状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ARRIVAL_STATUS } },
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

// ==================== 子表（到货明细） ====================

/** 新增/修改的表单 */
export function useArrivalLineFormSchema(): VbenFormSchema[] {
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
      fieldName: 'lineNo',
      label: '行号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入行号',
      },
    },
    {
      fieldName: 'sourceLineNo',
      label: '来源行号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源单据行号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'lotNo',
      label: '到货批次',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入到货批次号',
      },
    },
    {
      fieldName: 'supplierLotNo',
      label: '供应商批次',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入供应商批次号',
      },
    },
    {
      fieldName: 'arrivalQty',
      label: '到货数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入到货数量' },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'needQc',
      label: '需要质检',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: false,
    },
    {
      fieldName: 'iqcMode',
      label: '检验模式',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_IQC_MODE, 'string'),
        placeholder: '请选择来料检验模式',
      },
    },
    {
      fieldName: 'qualifiedQty',
      label: '合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入合格数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'rejectedQty',
      label: '不合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入不合格数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '明细状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_LINE_STATUS, 'string'),
        placeholder: '请选择明细状态',
      },
      defaultValue: 'PENDING',
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
export function useArrivalLineGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'lotNo',
      label: '到货批次',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入到货批次号',
      },
    },
    {
      fieldName: 'status',
      label: '明细状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ARRIVAL_LINE_STATUS, 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/** 列表的字段 */
export function useArrivalLineGridColumns(): VxeTableGridOptions<WmsArrivalApi.ArrivalLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'lotNo', title: '到货批次', minWidth: 140 },
    { field: 'supplierLotNo', title: '供应商批次', minWidth: 140 },
    { field: 'arrivalQty', title: '到货数量', minWidth: 110 },
    { field: 'uomCode', title: '计量单位', minWidth: 100 },
    {
      field: 'needQc',
      title: '需要质检',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'iqcMode',
      title: '检验模式',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_IQC_MODE } },
    },
    { field: 'qualifiedQty', title: '合格数量', minWidth: 110 },
    { field: 'rejectedQty', title: '不合格数量', minWidth: 110 },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_ARRIVAL_LINE_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
