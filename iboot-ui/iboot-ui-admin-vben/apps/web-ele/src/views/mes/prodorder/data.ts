import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdOrderApi } from '#/api/mes/prodorder';

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
  WORKSHOP_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, INT_PROPS, QTY_PROPS } from '../form-props';
import {
  BOM_RELATION_PROPS,
  PROD_DEMAND_RELATION_PROPS,
  ROUTE_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 物料与单位快照、BOM 与路线版本、进度数量、齐套状态、时间戳都由后端维护，不出现在表单里。
 * 状态同样不在表单里：工单状态只能由下达、挂起、关闭等动作推进，新建时后端兜底为 DRAFT。
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
      fieldName: 'orderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'demandId',
      label: '生产需求',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROD_DEMAND_RELATION_PROPS,
        placeholder: '手工建单可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'workshopId',
      label: '车间',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WORKSHOP_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '生产物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划生产数量' },
    },
    {
      fieldName: 'uomId',
      label: '计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'bomId',
      label: '锁定 BOM',
      rules: 'required',
      component: markRaw(RelationSelect),
      // BOM 属于所选物料，先选物料再挑版本
      dependencies: {
        triggerFields: ['matId'],
        componentProps: (values) => ({
          ...BOM_RELATION_PROPS,
          disabled: !values.matId,
          extraParams: { matId: values.matId },
          placeholder: values.matId ? '请选择 BOM 版本' : '请先选择生产物料',
        }),
      },
    },
    {
      fieldName: 'routeId',
      label: '锁定工艺路线',
      rules: 'required',
      component: markRaw(RelationSelect),
      dependencies: {
        triggerFields: ['matId'],
        componentProps: (values) => ({
          ...ROUTE_RELATION_PROPS,
          disabled: !values.matId,
          extraParams: { matId: values.matId },
          placeholder: values.matId ? '请选择工艺路线版本' : '请先选择生产物料',
        }),
      },
    },
    {
      fieldName: 'dueDate',
      label: '工单交期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择工单交期' },
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
      label: '生产工单数据',
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
      fieldName: 'orderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'matId',
      label: '生产物料',
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
      fieldName: 'workshopId',
      label: '车间',
      component: markRaw(RelationSelect),
      componentProps: WORKSHOP_RELATION_PROPS,
    },
    {
      fieldName: 'kitStatus',
      label: '齐套状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_KIT_STATUS, 'string'),
        placeholder: '请选择齐套状态',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_PROD_ORDER_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'dueDate',
      label: '工单交期',
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
export function useGridColumns(): VxeTableGridOptions<MesProdOrderApi.ProdOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'orderNo', title: '生产工单号', minWidth: 160 },
    { field: 'demandNo', title: '生产需求单号', minWidth: 160 },
    { field: 'plantName', title: '工厂', minWidth: 130 },
    { field: 'workshopName', title: '车间', minWidth: 130 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'matSpec', title: '规格型号', minWidth: 150 },
    { field: 'qty', title: '计划数量', minWidth: 110 },
    { field: 'completedQty', title: '已完成数量', minWidth: 110 },
    { field: 'scrapQty', title: '报废数量', minWidth: 110 },
    { field: 'uomCode', title: '单位', minWidth: 90 },
    { field: 'bomVersion', title: 'BOM 版本', minWidth: 110 },
    { field: 'routeVersion', title: '路线版本', minWidth: 110 },
    {
      field: 'dueDate',
      title: '工单交期',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'priority', title: '优先级', minWidth: 90 },
    {
      field: 'kitStatus',
      title: '齐套状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_KIT_STATUS },
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_PROD_ORDER_STATUS },
      },
    },
    {
      field: 'releaseTime',
      title: '下达时间',
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
