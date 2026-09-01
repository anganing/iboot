import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  UOM_RELATION_PROPS,
} from '#/views/mdm/relation-config';
import {
  BATCH_RELATION_PROPS,
  WH_RELATION_PROPS,
} from '#/views/wms/relation-config';

import { INT_PROPS, QTY_PROPS } from '../form-props';
import {
  OP_TASK_RELATION_PROPS,
  PROD_ORDER_RELATION_PROPS,
  WORK_ORDER_RELATION_PROPS,
} from '../relation-config';

/**
 * 主表新增/修改的表单。
 *
 * 生产工单号快照与项目维度由后端从生产工单继承；WMS 单号由 WMS 回写，都不在表单里。
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
      fieldName: 'reqNo',
      label: '申请单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PROD_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'reqType',
      label: '申请类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_MATERIAL_REQ_TYPE, 'string'),
        placeholder: '请选择申请类型',
      },
      defaultValue: 'ISSUE',
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WORK_ORDER_RELATION_PROPS,
        placeholder: '工单级申请可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...OP_TASK_RELATION_PROPS,
        placeholder: '非工序级申请可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_MATERIAL_REQ_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '业务自动触发时用于去重，手工建单可留空',
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
      fieldName: 'reqNo',
      label: '申请单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入申请单号',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'reqType',
      label: '申请类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_MATERIAL_REQ_TYPE, 'string'),
        placeholder: '请选择申请类型',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'wmsBizNo',
      label: 'WMS 单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入 WMS 单号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_MATERIAL_REQ_STATUS, 'string'),
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReq>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'reqNo', title: '申请单号', minWidth: 160 },
    {
      field: 'reqType',
      title: '申请类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_TYPE },
      },
    },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'whName', title: '仓库', minWidth: 140 },
    { field: 'projectName', title: '归属项目', minWidth: 140 },
    { field: 'wmsBizNo', title: 'WMS 单号', minWidth: 150 },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_STATUS },
      },
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（领退补料申请明细） ====================

/**
 * 明细新增/修改的表单。
 *
 * 物料、单位、批次号快照由后端补全；WMS 已处理数量由 WMS 回写，都不在表单里；
 * 父键 reqId 由父组件 setData 传入，不作为可见字段。
 */
export function useMaterialReqLineFormSchema(): VbenFormSchema[] {
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
        ...INT_PROPS,
        min: 1,
        placeholder: '同一申请单内不能重复',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '申请数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入申请数量' },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'batchId',
      label: '指定批次',
      component: markRaw(RelationSelect),
      componentProps: {
        ...BATCH_RELATION_PROPS,
        placeholder: '退料必须指定批次',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'mustIssueBeforeStart',
      label: '开工前发料',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: true,
    },
    {
      fieldName: 'status',
      label: '明细状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.MES_MATERIAL_REQ_LINE_STATUS,
          'string',
        ),
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

/** 明细列表的搜索表单 */
export function useMaterialReqLineGridFormSchema(): VbenFormSchema[] {
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
        options: getDictOptions(
          DICT_TYPE.MES_MATERIAL_REQ_LINE_STATUS,
          'string',
        ),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/** 明细列表的字段 */
export function useMaterialReqLineGridColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReqLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'qty', title: '申请数量', minWidth: 110 },
    { field: 'processedQty', title: 'WMS 已处理', minWidth: 110 },
    { field: 'uomCode', title: '单位', minWidth: 90 },
    { field: 'lotNo', title: '批次号', minWidth: 130 },
    {
      field: 'mustIssueBeforeStart',
      title: '开工前发料',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_LINE_STATUS },
      },
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
