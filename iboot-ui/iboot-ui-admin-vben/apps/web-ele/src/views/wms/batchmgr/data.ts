import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsBatchApi } from '#/api/wms/batchmgr';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  MAT_RELATION_PROPS,
  SUPPLIER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

/** 日期选择器的公共配置 */
const DATE_PROPS = {
  class: '!w-full',
  type: 'date' as const,
  valueFormat: 'YYYY-MM-DD',
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
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '无批次物料填默认批次',
      },
    },
    {
      fieldName: 'snNo',
      label: '序列号',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: 'SN 管控物料填写',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
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
      fieldName: 'furnaceLotNo',
      label: '炉批号',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入炉批号',
      },
    },
    {
      fieldName: 'produceDate',
      label: '生产日期',
      component: 'DatePicker',
      componentProps: { ...DATE_PROPS, placeholder: '请选择生产日期' },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'DatePicker',
      componentProps: { ...DATE_PROPS, placeholder: '请选择失效日期' },
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '批次数据',
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
      fieldName: 'lotNo',
      label: '批次号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'snNo',
      label: '序列号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入序列号',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
    },
    {
      fieldName: 'supplierLotNo',
      label: '供应商批次',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商批次号',
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
export function useGridColumns(): VxeTableGridOptions<WmsBatchApi.Batch>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lotNo', title: '批次号', minWidth: 150 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'snNo', title: '序列号', minWidth: 140 },
    { field: 'supplierCode', title: '供应商', minWidth: 140 },
    { field: 'supplierLotNo', title: '供应商批次', minWidth: 140 },
    { field: 'furnaceLotNo', title: '炉批号', minWidth: 130 },
    { field: 'produceDate', title: '生产日期', minWidth: 120 },
    { field: 'expiryDate', title: '失效日期', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
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
