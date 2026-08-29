import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmToolApi } from '#/api/mdm/tool';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { WC_RELATION_PROPS } from '../relation-config';

const TOOL_TYPE_OPTIONS = getDictOptions('mdm_tool_type', 'string');
const LIFE_TYPE_OPTIONS = getDictOptions('mdm_life_type', 'string');

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
        maxLength: 128,
      },
    },
    {
      fieldName: 'toolType',
      label: '工装类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: TOOL_TYPE_OPTIONS,
        placeholder: '请选择工装类型',
      },
    },
    {
      fieldName: 'wcId',
      label: '所属工作中心',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'qtyTotal',
      label: '保有数量',
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
      fieldName: 'lifeType',
      label: '寿命类型',
      component: 'Select',
      componentProps: {
        options: LIFE_TYPE_OPTIONS,
        clearable: true,
        placeholder: '请选择寿命类型',
      },
    },
    {
      fieldName: 'lifeLimit',
      label: '寿命上限',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '按寿命类型填写',
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
      fieldName: 'toolType',
      label: '工装类型',
      component: 'Select',
      componentProps: {
        options: TOOL_TYPE_OPTIONS,
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<MdmToolApi.Tool>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '编码', minWidth: 120 },
    { field: 'name', title: '名称', minWidth: 140 },
    {
      field: 'toolType',
      title: '工装类型',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'mdm_tool_type' } },
    },
    { field: 'wcCode', title: '工作中心编码', minWidth: 120 },
    { field: 'wcName', title: '工作中心名称', minWidth: 140 },
    { field: 'qtyTotal', title: '保有数量', minWidth: 100 },
    {
      field: 'lifeType',
      title: '寿命类型',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'mdm_life_type' } },
    },
    { field: 'lifeLimit', title: '寿命上限', minWidth: 100 },
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
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
