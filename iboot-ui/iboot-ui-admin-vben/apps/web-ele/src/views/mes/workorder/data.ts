import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWorkOrderApi } from '#/api/mes/workorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  PLANT_RELATION_PROPS,
  WC_RELATION_PROPS,
  WORKSHOP_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../form-props';
import { PROD_ORDER_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 工厂、车间、项目、生产工单号都从生产工单继承，让前端各填一遍只会制造不一致，所以不放进表单。
 * 状态也不在表单里：作业工单状态由下属工序任务的执行动作推进，新建时后端兜底为 PENDING。
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
      fieldName: 'workOrderNo',
      label: '作业工单号',
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
      fieldName: 'wcId',
      label: '工作中心',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'plannedQty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划数量' },
    },
    {
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划开始时间' },
    },
    {
      fieldName: 'plannedEndTime',
      label: '计划结束时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划结束时间' },
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
      fieldName: 'workOrderNo',
      label: '作业工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单号',
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
      fieldName: 'wcId',
      label: '工作中心',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
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
        options: getDictOptions(DICT_TYPE.MES_WORK_ORDER_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
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
export function useGridColumns(): VxeTableGridOptions<MesWorkOrderApi.WorkOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'plantName', title: '工厂', minWidth: 130 },
    { field: 'workshopName', title: '车间', minWidth: 130 },
    { field: 'wcName', title: '工作中心', minWidth: 140 },
    { field: 'plannedQty', title: '计划数量', minWidth: 110 },
    { field: 'completedQty', title: '已完成数量', minWidth: 110 },
    {
      field: 'plannedStartTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'plannedEndTime',
      title: '计划结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
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
        props: { type: DICT_TYPE.MES_WORK_ORDER_STATUS },
      },
    },
    { field: 'projectName', title: '归属项目', minWidth: 140 },
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
