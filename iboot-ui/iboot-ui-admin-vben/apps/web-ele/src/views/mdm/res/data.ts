import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResApi } from '#/api/mdm/res';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { MDM_RES_TYPE_OPTIONS } from '../constants';
import {
  CALENDAR_RELATION_PROPS,
  PLANT_RELATION_PROPS,
  WC_RELATION_PROPS,
  WORKSHOP_RELATION_PROPS,
} from '../relation-config';

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
      fieldName: 'wcId',
      label: '工作中心',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'resType',
      label: '资源类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: MDM_RES_TYPE_OPTIONS,
        placeholder: '请选择资源类型',
      },
    },
    {
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'efficiency',
      label: '效率系数',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0.0001,
        max: 10,
        precision: 4,
        placeholder: '默认 1.0',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'parallelCapacity',
      label: '可同时承载任务数',
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
      fieldName: 'resGroup',
      label: '资源组',
      component: 'Input',
      componentProps: {
        placeholder: '同组可互替排程',
        maxLength: 64,
      },
    },
    {
      fieldName: 'calendarId',
      label: '默认日历模板',
      component: markRaw(RelationSelect),
      componentProps: CALENDAR_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '所属工厂',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'workshopId',
      label: '所属车间',
      component: markRaw(RelationSelect),
      componentProps: WORKSHOP_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'wcId',
      label: '工作中心',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'resType',
      label: '资源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: MDM_RES_TYPE_OPTIONS,
        placeholder: '请选择资源类型',
      },
    },
    {
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入编码',
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注',
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
export function useGridColumns(): VxeTableGridOptions<MdmResApi.Res>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'wcCode', title: '工作中心编码', minWidth: 140 },
    { field: 'wcName', title: '工作中心名称', minWidth: 160 },
    {
      field: 'resType',
      title: '资源类型',
      minWidth: 120,
    },
    {
      field: 'code',
      title: '编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
