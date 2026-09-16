import type { RelationPickHandler } from '../order-form';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps, } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
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

/**
 * 新增/修改的表单。
 *
 * 没有"单据状态"项：状态由领域 Service 写死为新建，之后只能由提交、过账、取消三个动作推进，
 * 表单里放一个状态下拉框等于给状态开第二条写入路径。
 */
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
      fieldName: 'transferNo',
      label: '调拨单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入转移或调拨单号',
      },
    },
    {
      fieldName: 'transferType',
      label: '转移类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_TRANSFER_TYPE, 'string'),
        placeholder: '请选择转移类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromInvOrgId',
      label: '来源库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'fromWhId',
      label: '来源仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'fromProjectId',
      label: '来源项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'toProjectId',
      label: '目标项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'hookMode',
      label: '设备触发',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_HOOK_MODE, 'string'),
        placeholder: '请选择外部设备触发方式',
      },
      defaultValue: 'NONE',
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
      fieldName: 'transferNo',
      label: '调拨单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入转移或调拨单号',
      },
    },
    {
      fieldName: 'transferType',
      label: '转移类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_TRANSFER_TYPE, 'string'),
        placeholder: '请选择转移类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromWhId',
      label: '来源仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
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
export function useGridColumns(): VxeTableGridOptions<WmsTransferOrderApi.TransferOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'transferNo', title: '调拨单号', minWidth: 160, ...codeLinkColumn() },
    {
      field: 'transferType',
      title: '转移类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_TRANSFER_TYPE } },
    },
    { field: 'sourceBizNo', title: '来源单号', minWidth: 150 },
    { field: 'fromWhName', title: '来源仓库', minWidth: 140 },
    { field: 'toWhName', title: '目标仓库', minWidth: 140 },
    {
      field: 'hookMode',
      title: '设备触发',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_HOOK_MODE } },
    },
    {
      field: 'status',
      title: '单据状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ORDER_STATUS } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      // 行操作从 3 个增加到 6 个（明细、提交、过账、取消、修改、删除），列宽随之放宽
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（调拨明细） ====================

/**
 * 明细行弹窗的表单。
 *
 * 三个字段刻意不放进来：
 * - `lineNo` 由单据表单在提交时按顺序重排，让用户填会撞号，而行号参与过账幂等键，撞号会漏扣一行；
 * - `status` 行状态由领域 Service 写死为新建；
 * - `id` 界面新建的行没有主键，本地行靠 `_localKey` 定位。
 *
 * @param onPick 关联选择器回填快照的回调。物料编码、物料名称、批次号、基准单位编码这四个快照
 *   在 `wms_transfer_order_line` 上都是 NOT NULL 且无默认值，而转移单领域侧**不回填**物料快照
 *   （与入库单相反），前端不带过来这一行插不进库。
 */
export function useTransferOrderLineFormSchema(
  onPick?: RelationPickHandler,
): VbenFormSchema[] {
  return [
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
      fieldName: 'batchId',
      label: '批次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...BATCH_RELATION_PROPS,
        // 批次号是 NOT NULL 快照，只能跟着批次选择器带回来
        onChange: (row?: Record<string, any>) => onPick?.('batchId', row),
      },
    },
    {
      fieldName: 'containerId',
      label: '容器',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CONTAINER_RELATION_PROPS,
        placeholder: '散件留空',
        onChange: (row?: Record<string, any>) => onPick?.('containerId', row),
      },
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('fromLocId', row),
      },
    },
    {
      fieldName: 'toLocId',
      label: '目标库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('toLocId', row),
      },
    },
    {
      fieldName: 'fromStockScope',
      label: '来源库存范围',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_STOCK_SCOPE, 'string'),
        placeholder: '请选择来源库存范围',
      },
      defaultValue: 'COMMON',
    },
    {
      fieldName: 'toStockScope',
      label: '目标库存范围',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_STOCK_SCOPE, 'string'),
        placeholder: '请选择目标库存范围',
      },
      defaultValue: 'COMMON',
    },
    {
      fieldName: 'fromProjectId',
      label: '来源项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'toProjectId',
      label: '目标项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'fromProjectTaskId',
      label: '来源项目任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_TASK_RELATION_PROPS,
        placeholder: '无项目任务留空',
      },
    },
    {
      fieldName: 'toProjectTaskId',
      label: '目标项目任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_TASK_RELATION_PROPS,
        placeholder: '无项目任务留空',
      },
    },
    {
      fieldName: 'qualityStatusFrom',
      label: '来源质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择来源质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'qualityStatusTo',
      label: '目标质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择目标质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'uomId',
      label: '基准单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...UOM_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('uomId', row),
      },
    },
    {
      fieldName: 'planQty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划转移数量' },
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
export function useTransferOrderLineEditorColumns(): VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 130 },
    { field: 'fromLocCode', title: '来源库位', minWidth: 130 },
    { field: 'toLocCode', title: '目标库位', minWidth: 130 },
    { field: 'planQty', title: '计划数量', minWidth: 110 },
    { field: 'uomCode', title: '基准单位', minWidth: 100 },
    {
      field: 'qualityStatusFrom',
      title: '来源质量状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_QUALITY_STATUS },
      },
    },
    {
      field: 'qualityStatusTo',
      title: '目标质量状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_QUALITY_STATUS },
      },
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
export function useTransferOrderLineGridFormSchema(): VbenFormSchema[] {
  return [
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
      fieldName: 'status',
      label: '明细状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ORDER_LINE_STATUS, 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/**
 * 明细列表的字段（只读）。
 *
 * 没有勾选列与操作列：明细的增删改端点已撤销，这个列表只给已提交、已过账的单据看明细，
 * 要改明细得回到新建态的单据表单里整单改。
 */
export function useTransferOrderLineGridColumns(): VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'fromLocCode', title: '来源库位', minWidth: 130 },
    { field: 'toLocCode', title: '目标库位', minWidth: 130 },
    { field: 'planQty', title: '计划数量', minWidth: 110 },
    { field: 'movedQty', title: '已搬运数量', minWidth: 110 },
    { field: 'postedQty', title: '已过账数量', minWidth: 110 },
    { field: 'uomCode', title: '基准单位', minWidth: 100 },
    {
      field: 'qualityStatusFrom',
      title: '来源质量状态',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    {
      field: 'qualityStatusTo',
      title: '目标质量状态',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ORDER_LINE_STATUS } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
  ];
}
