import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvTxnApi } from '#/api/wms/invtxn';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';
import {
  MAT_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import {
  WH_RELATION_PROPS,
} from '../relation-config';

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
        options: getDictOptions(DICT_TYPE.WMS_TXN_TYPE, 'string'),
        placeholder: '请选择事务类型',
      },
    },
    {
      fieldName: 'ioType',
      label: '收发方向',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_IO_TYPE, 'string'),
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
    { type: 'seq', title: '序号', width: 60 },
    { field: 'txnNo', title: '事务编号', minWidth: 160, ...codeLinkColumn() },
    {
      field: 'txnType',
      title: '事务类型',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_TXN_TYPE } },
    },
    {
      field: 'ioType',
      title: '收发方向',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_IO_TYPE } },
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
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    {
      field: 'qualityStatusTo',
      title: '变更后质量',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '过账时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
  ];
}
