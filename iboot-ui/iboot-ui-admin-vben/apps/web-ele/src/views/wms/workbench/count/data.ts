import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { WH_RELATION_PROPS } from '../../relation-config';

/**
 * 盘点作业页的查询条件与列定义。
 *
 * 列定义没有复用 `views/wms/countorder/data.ts` 的 `useCountResultEditorColumns`：
 * 那份是单据页"录实盘"弹窗用的，只关心录入；作业页要在同一张表里同时承担录入与差异复核，
 * 多了差异率列、账面列的可见性规则也不同（见 {@link useCountLineColumns}）。
 * 让作业页反向依赖单据页的 data.ts 会把两个页面焊死，改一个必然波及另一个。
 */

/** 盘点单列表的搜索表单：列表已占满整页，沿用默认响应式栅格，不再固定单列 */
export function useCountOrderGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'countNo',
      label: '盘点单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '盘点状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ORDER_STATUS, 'string'),
        placeholder: '请选择盘点状态',
      },
    },
  ];
}

/**
 * 盘点单列表的列。
 *
 * 没有勾选列：作业页不做批量删除。操作列承载「明细」按钮——整行点击开抽屉已按规范 6.0 废弃，
 * 点行往往只是想看清某格内容，热区铺满整行会误触。只有 1 个固定动作，取规范 3.4 的 1 位档 100。
 */
export function useCountOrderGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrder>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'countNo', title: '盘点单号', minWidth: 150 },
    {
      field: 'countType',
      title: '盘点类型',
      width: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_COUNT_TYPE } },
    },
    { field: 'whName', title: '仓库', minWidth: 120 },
    {
      field: 'blindCountFlag',
      title: '盲盘',
      width: 70,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'status',
      title: '盘点状态',
      width: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ORDER_STATUS } },
    },
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}

/**
 * 盘点行（抽屉内作业表格）的列。
 *
 * @param blindCount 是否盲盘。盲盘时**账面数量列不渲染**——盘点员看得见账面就会照抄，
 *   盘点结果失去意义（`03-WMS仓储库存流程.md` 流程四）。
 *
 *   差异数量与差异率不受盲盘约束：它们只有提交实盘之后才有值，那时候数已经录完，
 *   照抄不再可能；而"确认差异"这个动作会直接生成盘盈入库单或盘亏出库单并过账，
 *   不让操作员看见差多少就点确认，是让他闭着眼改库存。
 */
export function useCountLineColumns(
  blindCount = false,
): VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 50 },
    { field: 'lineNo', title: '行号', width: 70 },
    { field: 'matCode', title: '物料编码', minWidth: 130 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    { field: 'lotNo', title: '批次号', minWidth: 120 },
    { field: 'locCode', title: '库位', minWidth: 120 },
    {
      field: 'bookQty',
      title: '账面数量',
      width: 100,
      visible: !blindCount,
    },
    {
      field: 'actualQty',
      title: '实盘数量',
      width: 160,
      slots: { default: 'actualQty' },
    },
    {
      field: 'diffQty',
      title: '差异数量',
      width: 100,
      slots: { default: 'diffQty' },
    },
    {
      field: 'diffRate',
      title: '差异率',
      width: 100,
      formatter: ({ row }) => formatDiffRate(row),
    },
    {
      field: 'remark',
      title: '盘点备注',
      minWidth: 160,
      slots: { default: 'lineRemark' },
    },
    {
      field: 'status',
      title: '明细状态',
      width: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_COUNT_LINE_STATUS },
      },
    },
  ];
}

/**
 * 差异率 = 差异数量 / 账面数量。
 *
 * 账面为 0 时不显示百分比：分母为 0 算出来是无穷大，写个"∞%"对盘点员没有任何指导意义，
 * 这种行看差异数量本身就够（账上没有、实物有多少就是盘盈多少）。
 */
export function formatDiffRate(row: WmsCountOrderApi.CountOrderLine) {
  if (row.diffQty === undefined || row.diffQty === null) {
    return '-';
  }
  const book = Number(row.bookQty ?? 0);
  const diff = Number(row.diffQty);
  if (book === 0) {
    return '-';
  }
  const rate = (diff / book) * 100;
  return `${diff > 0 ? '+' : ''}${rate.toFixed(2)}%`;
}
