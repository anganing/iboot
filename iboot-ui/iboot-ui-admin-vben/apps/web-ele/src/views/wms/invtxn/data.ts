import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvTxnApi } from '#/api/wms/invtxn';

import { markRaw } from 'vue';

import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  MAT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  UOM_RELATION_PROPS,
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
      fieldName: 'txnNo',
      label: '事务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入库存事务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_txn_type', 'string'),
        placeholder: '请选择事务类型',
      },
    },
    {
      fieldName: 'ioType',
      label: '收发方向',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_io_type', 'string'),
        placeholder: '请选择收发方向',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'bizLineNo',
      label: '来源行号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务行号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        placeholder: '入库场景可留空',
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        placeholder: '出库场景可留空',
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
        placeholder: '散件留空',
      },
    },
    {
      fieldName: 'uomId',
      label: '基准单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '事务数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入正数' },
    },
    {
      fieldName: 'beforeQty',
      label: '过账前数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入过账前现存数量' },
    },
    {
      fieldName: 'afterQty',
      label: '过账后数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入过账后现存数量' },
    },
    {
      fieldName: 'qualityStatusFrom',
      label: '变更前质量',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择变更前质量状态',
      },
    },
    {
      fieldName: 'qualityStatusTo',
      label: '变更后质量',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择变更后质量状态',
      },
    },
    {
      fieldName: 'projectIdFrom',
      label: '变更前项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'projectIdTo',
      label: '变更后项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入过账幂等键',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '库存流水数据',
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
      fieldName: 'txnNo',
      label: '事务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入事务编号',
      },
    },
    {
      fieldName: 'txnType',
      label: '事务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_txn_type', 'string'),
        placeholder: '请选择事务类型',
      },
    },
    {
      fieldName: 'ioType',
      label: '收发方向',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_io_type', 'string'),
        placeholder: '请选择收发方向',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
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
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'createTime',
      label: '过账时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<WmsInvTxnApi.InvTxn>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'txnNo', title: '事务编号', minWidth: 160 },
    {
      field: 'txnType',
      title: '事务类型',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: 'wms_txn_type' } },
    },
    {
      field: 'ioType',
      title: '收发方向',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_io_type' } },
    },
    { field: 'bizNo', title: '来源单号', minWidth: 150 },
    { field: 'bizLineNo', title: '来源行号', minWidth: 100 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 130 },
    { field: 'fromLocCode', title: '来源库位', minWidth: 130 },
    { field: 'toLocCode', title: '目标库位', minWidth: 130 },
    { field: 'qty', title: '事务数量', minWidth: 110 },
    { field: 'uomCode', title: '基准单位', minWidth: 100 },
    { field: 'beforeQty', title: '过账前数量', minWidth: 110 },
    { field: 'afterQty', title: '过账后数量', minWidth: 110 },
    {
      field: 'qualityStatusFrom',
      title: '变更前质量',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    {
      field: 'qualityStatusTo',
      title: '变更后质量',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '过账时间',
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
