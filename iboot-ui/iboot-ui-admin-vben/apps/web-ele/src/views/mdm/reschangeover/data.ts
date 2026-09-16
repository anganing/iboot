import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResChangeoverApi } from '#/api/mdm/reschangeover';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { RES_RELATION_PROPS } from '../relation-config';

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
      label: '生效资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...RES_RELATION_PROPS,
        allowClear: true,
      },
      help: '不选则按资源组生效',
    },
    {
      fieldName: 'resGroup',
      label: '资源组',
      component: 'Input',
      componentProps: {
        placeholder: 'resId 为空时必填',
        maxLength: 64,
      },
    },
    {
      fieldName: 'fromGroup',
      label: '换出分组',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '如颜色/材质/型号族',
        maxLength: 64,
      },
    },
    {
      fieldName: 'toGroup',
      label: '换入分组',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '如颜色/材质/型号族',
        maxLength: 64,
      },
    },
    {
      fieldName: 'changeoverMinutes',
      label: '换型分钟数',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
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
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'fromGroup',
      label: '换出分组',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入换出分组',
      },
    },
    {
      fieldName: 'toGroup',
      label: '换入分组',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入换入分组',
      },
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
export function useGridColumns(): VxeTableGridOptions<MdmResChangeoverApi.ResChangeover>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'resCode', title: '资源编码', minWidth: 120 },
    { field: 'resName', title: '资源名称', minWidth: 140 },
    { field: 'resGroup', title: '资源组', minWidth: 120 },
    { field: 'fromGroup', title: '换出分组', minWidth: 120 },
    { field: 'toGroup', title: '换入分组', minWidth: 120 },
    { field: 'changeoverMinutes', title: '换型分钟数', minWidth: 110 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 120 },
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
