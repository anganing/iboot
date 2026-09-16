import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { WH_RELATION_PROPS } from '../../relation-config';

/**
 * 拣选发货作业页的查询条件与列定义。
 *
 * 列定义没有复用 `views/wms/outordermgr/data.ts`：单据页的明细列是只读的，作业页要逐行改拣选
 * 数量、还要显示当前库存可用量。让作业页反向依赖单据页的 data.ts 会把两个页面焊死。
 */

/** 出库单列表的搜索表单：列表已占满整页，沿用默认响应式栅格，不再固定单列 */
export function useOutOrderGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'outNo',
      label: '出库单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入出库单号',
      },
    },
    {
      fieldName: 'whId',
      label: '来源仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'bizType',
      label: '出库类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_OUT_BIZ_TYPE, 'string'),
        placeholder: '请选择出库业务类型',
      },
    },
    {
      fieldName: 'status',
      label: '单据状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ORDER_STATUS, 'string'),
        placeholder: '请选择单据状态',
      },
    },
  ];
}

/**
 * 出库单列表的列。
 *
 * 没有勾选列：作业页不做批量删除。操作列承载「明细」按钮——整行点击开抽屉已按规范 6.0 废弃，
 * 点行往往只是想看清某格内容，热区铺满整行会误触。只有 1 个固定动作，取规范 3.4 的 1 位档 100。
 */
export function useOutOrderGridColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrder>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'outNo', title: '出库单号', minWidth: 150 },
    {
      field: 'bizType',
      title: '出库类型',
      width: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_OUT_BIZ_TYPE },
      },
    },
    { field: 'whName', title: '来源仓库', minWidth: 120 },
    {
      field: 'status',
      title: '单据状态',
      width: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ORDER_STATUS } },
    },
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}

/**
 * 出库明细（抽屉内作业表格）的列。
 *
 * 批次与库位是**只读**的：后端没有拣选分配动作，`submitOutOrder` 只推状态、不按 PICK 策略选批次
 * 与库位，所以行上的批次与库位就是建单时指定的值。缺任意一个，过账都会失败
 * （无库位抛"出库明细必须指定来源库位"；无批次会在可用量校验时匹配不到余额行）。
 *
 * 拣选数量走 `planQty` 插槽：后端 `confirmOutOrder` 的过账量是 `planQty - postedQty`，
 * 请求 VO 上没有任何"按行提交拣选数量"的入参，所以"确认拣选数量"的唯一落点就是改计划数量，
 * 而 `updateOutOrder` 只放行新建态——非新建态的单据只能核对，改不了。
 *
 * 不展示"已分配数量"与"已拣选数量"：当前后端 `allocatedQty` 恒为 0（没有分配动作写它），
 * `pickedQty` 是过账后才由 `confirmOutOrder` 赋成与已过账量相同的值，两列摆出来只会误导操作员
 * 以为存在一个分配环节。真实进度看"已过账数量"。
 */
export function useOutOrderLineColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'lineNo', title: '行号', width: 70 },
    { field: 'matCode', title: '物料编码', minWidth: 130 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    {
      field: 'lotNo',
      title: '指定批次',
      minWidth: 130,
      slots: { default: 'lotNo' },
    },
    {
      field: 'fromLocCode',
      title: '来源库位',
      minWidth: 120,
      slots: { default: 'fromLocCode' },
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      width: 90,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_QUALITY_STATUS },
      },
    },
    {
      field: 'planQty',
      title: '拣选数量',
      width: 150,
      slots: { default: 'planQty' },
    },
    {
      field: 'availableQty',
      title: '可用量',
      width: 110,
      slots: { default: 'availableQty' },
    },
    { field: 'postedQty', title: '已过账', width: 90 },
    { field: 'uomCode', title: '单位', width: 70 },
    {
      field: 'status',
      title: '明细状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_ORDER_LINE_STATUS },
      },
    },
  ];
}
