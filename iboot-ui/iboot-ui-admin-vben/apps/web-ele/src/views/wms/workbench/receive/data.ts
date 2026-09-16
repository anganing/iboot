import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { WH_RELATION_PROPS } from '../../relation-config';

/**
 * 收货上架作业页的查询条件与列定义。
 *
 * 列定义没有复用 `views/wms/arrival/data.ts`：单据页的明细列是给"看"用的只读列，
 * 作业页要在同一张表里逐行改数量与批次、还要显示当前库存可用量。让作业页反向依赖单据页的
 * data.ts 会把两个页面焊死，改一个必然波及另一个。
 */

/** 到货单列表的搜索表单：列表已占满整页，沿用默认响应式栅格，不再固定单列 */
export function useArrivalGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'arrivalNo',
      label: '到货单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入到货单号',
      },
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
  ];
}

/**
 * 到货单列表的列。
 *
 * 没有勾选列：作业页不做批量删除，勾选列在这里没有用处。
 * 操作列承载「明细」按钮——整行点击开抽屉的交互已按规范 6.0 废弃，操作员点行往往只是想看清
 * 某格内容，热区铺满整行会误触。只有 1 个固定动作，按规范 3.4 取 1 位档 100。
 */
export function useArrivalGridColumns(): VxeTableGridOptions<WmsArrivalApi.Arrival>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'arrivalNo', title: '到货单号', minWidth: 150 },
    { field: 'supplierName', title: '供应商', minWidth: 140 },
    { field: 'whName', title: '目标仓库', minWidth: 120 },
    { field: 'expectDate', title: '预计到货', width: 110 },
    {
      field: 'status',
      title: '到货状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_ARRIVAL_STATUS },
      },
    },
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}

/**
 * 到货明细（抽屉内作业表格）的列。
 *
 * 数量、到货批次、供应商批次三列走插槽：草稿态时渲染输入控件让操作员按实收改，
 * 确认到货之后渲染纯文本——`confirmArrival` 会逐行注册批次并回写 `lotNo`，
 * 之后行上的数量与批次都是既成事实，`updateArrival` 也只放行草稿态。
 *
 * "当前可用量"不是到货单上的字段，是按目标仓库 + 物料现查的库存可用量，
 * 给操作员一个账实对照的锚点（`03-WMS仓储库存流程.md` 作业导向页面）。
 */
export function useReceiveLineColumns(): VxeTableGridOptions<WmsArrivalApi.ArrivalLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'lineNo', title: '行号', width: 70 },
    { field: 'matCode', title: '物料编码', minWidth: 130 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    {
      field: 'arrivalQty',
      title: '到货数量',
      width: 150,
      slots: { default: 'arrivalQty' },
    },
    { field: 'uomCode', title: '单位', width: 70 },
    {
      field: 'availableQty',
      title: '当前可用量',
      width: 110,
      slots: { default: 'availableQty' },
    },
    {
      field: 'lotNo',
      title: '到货批次',
      minWidth: 140,
      slots: { default: 'lotNo' },
    },
    {
      field: 'supplierLotNo',
      title: '供应商批次',
      minWidth: 140,
      slots: { default: 'supplierLotNo' },
    },
    {
      field: 'needQc',
      title: '需要质检',
      width: 90,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'iqcMode',
      title: '检验模式',
      width: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_IQC_MODE } },
    },
    {
      field: 'status',
      title: '明细状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_ARRIVAL_LINE_STATUS },
      },
    },
  ];
}
