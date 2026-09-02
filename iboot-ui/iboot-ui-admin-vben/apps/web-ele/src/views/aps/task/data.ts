import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskApi } from '#/api/aps/task';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  PLANT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  RES_RELATION_PROPS,
  WORKSHOP_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS } from '../form-props';
import { APS_VERSION_RELATION_PROPS } from '../relation-config';

/** 人工调整的表单：只允许改资源、计划时间与执行顺序 */
export function useAdjustFormSchema(): VbenFormSchema[] {
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
      fieldName: 'taskNo',
      label: '任务编号',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'opName',
      label: '工序',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'sequenceNo',
      label: '执行顺序号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
      },
      defaultValue: 1,
    },
    {
      fieldName: 'startTime',
      label: '计划开始时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划开始时间' },
    },
    {
      fieldName: 'endTime',
      label: '计划结束时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划结束时间' },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入调整原因',
        rows: 2,
      },
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'versionId',
      label: '排程版本',
      component: markRaw(RelationSelect),
      componentProps: APS_VERSION_RELATION_PROPS,
    },
    {
      fieldName: 'taskNo',
      label: '任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入任务编号',
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
      fieldName: 'opCode',
      label: '工序编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序编码',
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
      fieldName: 'projectId',
      label: '项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'lockedFlag',
      label: '是否锁定',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否锁定',
      },
    },
    {
      fieldName: 'startTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<ApsTaskApi.Task>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '任务编号', minWidth: 160 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 150 },
    { field: 'opCode', title: '工序编码', minWidth: 110 },
    { field: 'opName', title: '工序名称', minWidth: 130 },
    { field: 'resCode', title: '资源编码', minWidth: 110 },
    { field: 'resName', title: '资源名称', minWidth: 130 },
    { field: 'plantName', title: '工厂', minWidth: 120 },
    { field: 'workshopName', title: '车间', minWidth: 120 },
    { field: 'projectName', title: '项目', minWidth: 130 },
    {
      field: 'startTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '计划结束时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    { field: 'durationMinutes', title: '占用分钟', minWidth: 100 },
    { field: 'sequenceNo', title: '顺序号', minWidth: 90 },
    {
      field: 'lockedFlag',
      title: '是否锁定',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_TASK_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
