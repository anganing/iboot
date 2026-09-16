import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResCapabilityApi } from '#/api/mdm/rescapability';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { OP_RELATION_PROPS, RES_RELATION_PROPS } from '../relation-config';

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
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'opId',
      label: '标准工序',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_RELATION_PROPS,
    },
    {
      fieldName: 'preferredFlag',
      label: '首选资源',
      component: 'Switch',
      componentProps: {
        checkedValue: true,
        uncheckedValue: false,
      },
      defaultValue: false,
    },
    {
      fieldName: 'efficiency',
      label: '专属效率系数',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0.0001,
        max: 10,
        precision: 4,
        placeholder: '为空时继承资源效率',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'opId',
      label: '标准工序',
      component: markRaw(RelationSelect),
      componentProps: OP_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MdmResCapabilityApi.ResCapability>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'resCode', title: '资源编码', minWidth: 140 },
    { field: 'resName', title: '资源名称', minWidth: 160 },
    { field: 'opCode', title: '工序编码', minWidth: 140 },
    { field: 'opName', title: '工序名称', minWidth: 160 },
    {
      field: 'preferredFlag',
      title: '首选资源',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'efficiency', title: '专属效率系数', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
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
