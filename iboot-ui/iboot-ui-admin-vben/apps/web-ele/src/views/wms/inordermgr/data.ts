import type { RelationPickHandler } from '../order-form';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  CUSTOMER_RELATION_PROPS,
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PLANT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
  SUPPLIER_RELATION_PROPS,
  UOM_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import {
  ARRIVAL_RELATION_PROPS,
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
 *
 * @param onPick 关联选择器回填快照的回调，目标仓库要靠它把仓库编码带回来（后端该字段必填）
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
      fieldName: 'inNo',
      label: '入库单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入入库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_IN_BIZ_TYPE, 'string'),
        placeholder: '请选择入库业务类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入直接来源业务单号',
      },
    },
    {
      fieldName: 'originNo',
      label: '原始单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入原始来源单号',
      },
    },
    {
      fieldName: 'arrivalId',
      label: '到货登记',
      component: markRaw(RelationSelect),
      componentProps: {
        ...ARRIVAL_RELATION_PROPS,
        placeholder: '非采购入库可留空',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
    },
    {
      fieldName: 'customerId',
      label: '客户',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CUSTOMER_RELATION_PROPS,
        placeholder: '销售退货入库时选择',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '完工入库时填写',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '目标库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '目标工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '目标仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WH_RELATION_PROPS,
        // 后端 whCode 是必填快照，选完仓库要把编码一起带回去
        onChange: (row?: Record<string, any>) => onPick?.('whId', row),
      },
    },
    {
      fieldName: 'fromInvOrgId',
      label: '来源库存组织',
      component: markRaw(RelationSelect),
      componentProps: {
        ...INV_ORG_RELATION_PROPS,
        placeholder: '非调拨入库可留空',
      },
    },
    {
      fieldName: 'fromWhId',
      label: '来源仓库',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WH_RELATION_PROPS,
        placeholder: '非调拨入库可留空',
      },
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
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择入库质量状态',
      },
      defaultValue: 'QUALIFIED',
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
        placeholder: '请输入备注或人工原因',
        rows: 2,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'inNo',
      label: '入库单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入入库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_IN_BIZ_TYPE, 'string'),
        placeholder: '请选择入库业务类型',
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
      fieldName: 'whId',
      label: '目标仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: SUPPLIER_RELATION_PROPS,
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
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
export function useGridColumns(): VxeTableGridOptions<WmsInOrderApi.InOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'inNo', title: '入库单号', minWidth: 160 },
    {
      field: 'bizType',
      title: '业务类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_IN_BIZ_TYPE } },
    },
    { field: 'sourceBizNo', title: '来源单号', minWidth: 150 },
    { field: 'arrivalArrivalNo', title: '到货单号', minWidth: 150 },
    { field: 'whName', title: '目标仓库', minWidth: 140 },
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
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
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
      width: 360,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（入库明细） ====================

/**
 * 明细行弹窗的表单。
 *
 * 三个字段刻意不放进来：
 * - `lineNo` 由表单在提交时按顺序重排，让用户填会撞号，而行号参与过账幂等键，撞号会漏扣一行；
 * - `status` 行状态由领域 Service 写死为新建；
 * - `id` 界面新建的行没有主键，本地行靠 `_localKey` 定位。
 *
 * @param onPick 关联选择器回填快照的回调，物料编码、库位编码这些可读字段靠它带回明细表格
 */
export function useInOrderLineFormSchema(
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
      component: markRaw(RelationSelect),
      componentProps: {
        ...BATCH_RELATION_PROPS,
        placeholder: '留空由入库时按批次号注册',
        onChange: (row?: Record<string, any>) => onPick?.('batchId', row),
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '不选批次时填到货批次号，留空取默认批次',
      },
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
      fieldName: 'locId',
      label: '目标库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        onChange: (row?: Record<string, any>) => onPick?.('locId', row),
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
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择入库质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'planQty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划入库数量' },
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: {
        ...SUPPLIER_RELATION_PROPS,
        placeholder: '采购入库时选择',
        onChange: (row?: Record<string, any>) => onPick?.('supplierId', row),
      },
    },
    {
      fieldName: 'supplierLotNo',
      label: '供应商批次',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入供应商批次号',
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
export function useInOrderLineEditorColumns(): VxeTableGridOptions<WmsInOrderApi.InOrderLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 120 },
    { field: 'locCode', title: '目标库位', minWidth: 130 },
    { field: 'planQty', title: '计划数量', minWidth: 110 },
    { field: 'uomCode', title: '计量单位', minWidth: 100 },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_QUALITY_STATUS },
      },
    },
    { field: 'supplierLotNo', title: '供应商批次', minWidth: 130 },
    { field: 'remark', title: '备注', minWidth: 120 },
    {
      title: '操作',
      width: 130,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 列表的搜索表单 */
export function useInOrderLineGridFormSchema(): VbenFormSchema[] {
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
export function useInOrderLineGridColumns(): VxeTableGridOptions<WmsInOrderApi.InOrderLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'locCode', title: '目标库位', minWidth: 130 },
    { field: 'planQty', title: '计划数量', minWidth: 110 },
    { field: 'completedQty', title: '已完成数量', minWidth: 110 },
    { field: 'postedQty', title: '已过账数量', minWidth: 110 },
    { field: 'uomCode', title: '计量单位', minWidth: 100 },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
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
