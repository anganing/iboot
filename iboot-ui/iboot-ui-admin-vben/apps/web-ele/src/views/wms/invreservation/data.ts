import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvReservationApi } from '#/api/wms/invreservation';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS } from '#/views/mdm/relation-config';

import { LOC_RELATION_PROPS, WH_RELATION_PROPS } from '../relation-config';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'reservationNo',
      label: '预留编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入预留编号',
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
      fieldName: 'status',
      label: '预留状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_RESERVATION_STATUS, 'string'),
        placeholder: '请选择预留状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsInvReservationApi.InvReservation>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    {
      field: 'reservationNo',
      title: '预留编号',
      minWidth: 160,
      // 预留编号有唯一索引 uk_wms_inv_reservation_no_deleted，是唯一能定位本行的业务编码；
      // 物料/批次/库位只是占用维度，同一组合会被多张单预留，拿它们做链接会串记录。
      ...codeLinkColumn(),
    },
    { field: 'bizNo', title: '来源单号', minWidth: 150 },
    { field: 'bizLineNo', title: '来源行号', minWidth: 100 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 130 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    { field: 'qty', title: '预留数量', minWidth: 110 },
    { field: 'releasedQty', title: '已处理数量', minWidth: 110 },
    {
      field: 'remainQty',
      title: '剩余数量',
      minWidth: 110,
      formatter: ({ row }) =>
        Number(row.qty ?? 0) - Number(row.releasedQty ?? 0),
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    {
      field: 'status',
      title: '预留状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_RESERVATION_STATUS },
      },
    },
    {
      field: 'expireTime',
      title: '失效时间',
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
      // 本列只有「释放」1 个固定动作，按规范 3.4 档位表取 100；原先的 160 是留白不是位数
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
