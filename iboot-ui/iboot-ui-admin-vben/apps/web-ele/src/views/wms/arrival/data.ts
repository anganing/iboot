import type { RelationPickHandler } from '../order-form';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';
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

/**
 * 新增/修改的表单。
 *
 * 两个字段刻意不放进来：
 * - `status` 到货状态由领域 Service 写死为草稿，之后只能由确认到货、生成入库单两个动作推进，
 *   表单里放一个状态下拉框等于给状态开第二条写入路径；
 * - `arrivalTime` 实际到货时间是确认到货动作的留痕，请求 VO 上有这个字段但创建 BO 里没有，
 *   表单填了也会被丢弃。
 *
 * @param onPick 关联选择器回填快照的回调。供应商编码与名称是 `wms_arrival` 上的快照列、
 *   领域侧不回填，不带过来列表里的供应商两列就是空的。
 */
export function useFormSchema(onPick?: RelationPickHandler): VbenFormSchema[] {
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
      componentProps: {
        ...SUPPLIER_RELATION_PROPS,
        // 供应商编码与名称是必带的快照，选完供应商要把两个可读字段一起带回去
        onChange: (row?: Record<string, any>) => onPick?.('supplierId', row),
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
    { field: 'arrivalNo', title: '到货单号', minWidth: 160, ...codeLinkColumn() },
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
      // 行操作从 3 个增加到 5 个（明细、确认到货、生成入库单、修改、删除），列宽随之放宽
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（到货明细） ====================

/**
 * 明细行弹窗的表单。
 *
 * 五个字段刻意不放进来：
 * - `lineNo` 由单据表单在提交时按顺序重排，让用户填会撞号，而行号参与生成入库单的幂等键，
 *   撞号会让两批明细拼出同一个键、第二批静默命中原单；
 * - `status` 行状态由领域 Service 写死为待处理，之后由确认到货按是否需要质检分流；
 * - `qualifiedQty` / `rejectedQty` 是质检结论回写的，创建时归零；
 * - `id` 界面新建的行没有主键，本地行靠 `_localKey` 定位。
 *
 * @param onPick 关联选择器回填快照的回调。物料编码、名称、规格与单位编码由领域 Service 从
 *   MaterialApi、UomApi 批量取后落库，这里带回来只为让明细表格显示可读字段而不是裸 ID。
 */
export function useArrivalLineFormSchema(
  onPick?: RelationPickHandler,
): VbenFormSchema[] {
  return [
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
      componentProps: {
        ...MAT_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('matId', row),
      },
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
      componentProps: {
        ...UOM_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('uomId', row),
      },
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

/**
 * 单据表单里明细区的列。
 *
 * 数据源是本地数组，不发请求：明细随单头一次提交，改单时后端整批替换旧行。
 */
export function useArrivalLineEditorColumns(): VxeTableGridOptions<WmsArrivalApi.ArrivalLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '到货批次', minWidth: 130 },
    { field: 'supplierLotNo', title: '供应商批次', minWidth: 130 },
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
    { field: 'remark', title: '备注', minWidth: 120 },
    {
      title: '操作',
      width: 100,
      fixed: 'right',
      slots: { default: 'actions' },
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

/**
 * 明细列表的字段（只读）。
 *
 * 没有操作列：明细的增删改端点已撤销，这个列表只给已确认到货的单据看明细，
 * 要改明细得回到草稿态的单据表单里整单改。
 *
 * 勾选列保留，但它不是为了批量删除，而是"按选中明细生成入库单"：后端
 * `create-in-order` 支持只挑一部分待入库明细生成，混着待质检明细的到货单靠它分批入库。
 */
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
  ];
}
