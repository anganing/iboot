import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdDemandApi } from '#/api/mes/proddemand';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  MAT_RELATION_PROPS,
  PLANT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
  UOM_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, INT_PROPS, QTY_PROPS } from '../form-props';

/**
 * 新增/修改的表单。
 *
 * 物料编码、名称、规格与单位编码是后端从 mdm 门面取的快照，不出现在表单里。
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
      fieldName: 'demandNo',
      label: '需求单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_DEMAND_SOURCE_TYPE, 'string'),
        placeholder: '请选择来源类型',
      },
      defaultValue: 'MANUAL',
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '如销售订单号、ERP 需求号',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '需求物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '需求数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入需求数量' },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'dueDate',
      label: '需求交期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择需求交期' },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...INT_PROPS, placeholder: '数值越大越优先' },
      defaultValue: 0,
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '无项目请选择占位项目',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      // 项目任务隶属于项目，先选项目再挑任务，否则会挑到别的项目的任务
      dependencies: {
        triggerFields: ['projectId'],
        componentProps: (values) => ({
          ...PROJECT_TASK_RELATION_PROPS,
          disabled: !values.projectId,
          extraParams: { projectId: values.projectId },
          placeholder: values.projectId ? '请选择项目任务' : '请先选择项目',
        }),
      },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_DEMAND_STATUS, 'string'),
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
        placeholder: '外部系统下发时用于去重，手工建单可留空',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '生产需求数据',
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
      fieldName: 'demandNo',
      label: '需求单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入需求单号',
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
      fieldName: 'matId',
      label: '需求物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'sourceType',
      label: '来源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DEMAND_SOURCE_TYPE, 'string'),
        placeholder: '请选择来源类型',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DEMAND_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'dueDate',
      label: '需求交期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<MesProdDemandApi.ProdDemand>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'demandNo', title: '需求单号', minWidth: 160 },
    {
      field: 'sourceType',
      title: '来源类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DEMAND_SOURCE_TYPE },
      },
    },
    { field: 'sourceNo', title: '来源单号', minWidth: 150 },
    { field: 'plantName', title: '工厂', minWidth: 130 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'qty', title: '需求数量', minWidth: 110 },
    { field: 'uomCode', title: '单位', minWidth: 90 },
    {
      field: 'dueDate',
      title: '需求交期',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'priority', title: '优先级', minWidth: 90 },
    { field: 'projectName', title: '归属项目', minWidth: 140 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DEMAND_STATUS },
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
