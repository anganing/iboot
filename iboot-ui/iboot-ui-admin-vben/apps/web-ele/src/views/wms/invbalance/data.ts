import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvBalanceApi } from '#/api/wms/invbalance';

import { markRaw } from 'vue';

import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PLANT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import {
  BATCH_RELATION_PROPS,
  CONTAINER_RELATION_PROPS,
  LOC_RELATION_PROPS,
  WH_RELATION_PROPS,
} from '../relation-config';

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
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'batchId',
      label: '批次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: BATCH_RELATION_PROPS,
    },
    {
      fieldName: 'containerId',
      label: '容器',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CONTAINER_RELATION_PROPS,
        placeholder: '散件库存留空',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_stock_scope', 'string'),
        placeholder: '请选择库存范围',
      },
      defaultValue: 'COMMON',
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_TASK_RELATION_PROPS,
        placeholder: '无项目任务留空',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'qtyOnhand',
      label: '现存数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入现存数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyReserved',
      label: '预留数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入预留数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyFrozen',
      label: '冻结数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入冻结数量' },
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
      label: '库存余额数据',
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
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编码',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
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
      fieldName: 'qualityStatus',
      label: '质量状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择质量状态',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_stock_scope', 'string'),
        placeholder: '请选择库存范围',
      },
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
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
export function useGridColumns(): VxeTableGridOptions<WmsInvBalanceApi.InvBalance>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 130 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    { field: 'qtyOnhand', title: '现存数量', minWidth: 110 },
    { field: 'qtyReserved', title: '预留数量', minWidth: 110 },
    { field: 'qtyFrozen', title: '冻结数量', minWidth: 110 },
    {
      field: 'qtyAvailable',
      title: '可用数量',
      minWidth: 110,
      formatter: ({ row }) =>
        Number(row.qtyOnhand ?? 0) -
        Number(row.qtyReserved ?? 0) -
        Number(row.qtyFrozen ?? 0),
    },
    { field: 'uomCode', title: '基准单位', minWidth: 100 },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    {
      field: 'stockScope',
      title: '库存范围',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_stock_scope' } },
    },
    { field: 'projectName', title: '归属项目', minWidth: 140 },
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
