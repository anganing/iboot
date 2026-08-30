import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

import { markRaw } from 'vue';

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
  BATCH_RELATION_PROPS,
  CONTAINER_RELATION_PROPS,
  LOC_RELATION_PROPS,
  STRATEGY_RELATION_PROPS,
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
      fieldName: 'outNo',
      label: '出库单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入出库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_out_biz_type', 'string'),
        placeholder: '请选择出库业务类型',
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
      fieldName: 'customerId',
      label: '客户',
      component: markRaw(RelationSelect),
      componentProps: CUSTOMER_RELATION_PROPS,
    },
    {
      fieldName: 'supplierId',
      label: '供应商',
      component: markRaw(RelationSelect),
      componentProps: {
        ...SUPPLIER_RELATION_PROPS,
        placeholder: '采购退货出库时选择',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '生产领料时填写',
      },
    },
    {
      fieldName: 'materialReqNo',
      label: '领料申请号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入领退补料申请单号',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '来源库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '来源工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '来源仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织',
      component: markRaw(RelationSelect),
      componentProps: {
        ...INV_ORG_RELATION_PROPS,
        placeholder: '非调拨出库可留空',
      },
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WH_RELATION_PROPS,
        placeholder: '非调拨出库可留空',
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
      fieldName: 'pickStrategyId',
      label: '拣选策略',
      component: markRaw(RelationSelect),
      componentProps: {
        ...STRATEGY_RELATION_PROPS,
        placeholder: '按默认策略时留空',
      },
    },
    {
      fieldName: 'hookMode',
      label: '设备触发',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_hook_mode', 'string'),
        placeholder: '请选择外部设备触发方式',
      },
      defaultValue: 'NONE',
    },
    {
      fieldName: 'status',
      label: '单据状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_order_status', 'string'),
        placeholder: '请选择单据状态',
      },
      defaultValue: 'DRAFT',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '出库单数据',
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
      fieldName: 'outNo',
      label: '出库单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入出库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_out_biz_type', 'string'),
        placeholder: '请选择出库业务类型',
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
      label: '来源仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'customerId',
      label: '客户',
      component: markRaw(RelationSelect),
      componentProps: CUSTOMER_RELATION_PROPS,
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
        options: getDictOptions('wms_order_status', 'string'),
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
export function useGridColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'outNo', title: '出库单号', minWidth: 160 },
    {
      field: 'bizType',
      title: '业务类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: 'wms_out_biz_type' } },
    },
    { field: 'sourceBizNo', title: '来源单号', minWidth: 150 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 140 },
    { field: 'whName', title: '来源仓库', minWidth: 140 },
    {
      field: 'hookMode',
      title: '设备触发',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_hook_mode' } },
    },
    {
      field: 'status',
      title: '单据状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_order_status' } },
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（出库明细） ====================

/** 新增/修改的表单 */
export function useOutOrderLineFormSchema(): VbenFormSchema[] {
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
      fieldName: 'lineNo',
      label: '行号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入行号',
      },
    },
    {
      fieldName: 'sourceLineNo',
      label: '来源行号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源明细行号',
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
      component: markRaw(RelationSelect),
      componentProps: {
        ...BATCH_RELATION_PROPS,
        placeholder: '系统分配时可留空',
      },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'fromLocId',
      label: '来源库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'toLocId',
      label: '目标库位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...LOC_RELATION_PROPS,
        placeholder: '发货区落位时填写',
      },
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
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择要求的质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'planQty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划出库数量' },
    },
    {
      fieldName: 'allocatedQty',
      label: '已分配数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入已分配数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'pickedQty',
      label: '已拣选数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入已拣选数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'postedQty',
      label: '已过账数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入已过账数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '明细状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_order_status', 'string'),
        placeholder: '请选择明细状态',
      },
      defaultValue: 'DRAFT',
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
export function useOutOrderLineGridFormSchema(): VbenFormSchema[] {
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
        options: getDictOptions('wms_order_status', 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/** 列表的字段 */
export function useOutOrderLineGridColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'fromLocCode', title: '来源库位', minWidth: 130 },
    { field: 'planQty', title: '计划数量', minWidth: 110 },
    { field: 'allocatedQty', title: '已分配数量', minWidth: 110 },
    { field: 'pickedQty', title: '已拣选数量', minWidth: 110 },
    { field: 'postedQty', title: '已过账数量', minWidth: 110 },
    { field: 'uomCode', title: '计量单位', minWidth: 100 },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_order_status' } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
