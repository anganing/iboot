import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmUomConvApi } from '#/api/mdm/uomconv';

import { markRaw } from 'vue';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { MAT_RELATION_PROPS, UOM_RELATION_PROPS } from '../relation-config';

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
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'fromUomId',
      label: '来源计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...UOM_RELATION_PROPS,
        dialogTitle: '选择来源计量单位',
        placeholder: '请选择来源计量单位',
      },
    },
    {
      fieldName: 'toUomId',
      label: '目标计量单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...UOM_RELATION_PROPS,
        dialogTitle: '选择目标计量单位',
        placeholder: '请选择目标计量单位',
      },
    },
    {
      fieldName: 'ratio',
      label: '换算比例',
      help: '1个来源单位 × 换算比例 = 目标单位数量，例如 kg 换算为 件 时填 2，表示 1kg = 2件',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入换算比例',
        step: 0.01,
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
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'fromUomId',
      label: '来源计量单位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...UOM_RELATION_PROPS,
        dialogTitle: '选择来源计量单位',
        placeholder: '请选择来源计量单位',
      },
    },
    {
      fieldName: 'toUomId',
      label: '目标计量单位',
      component: markRaw(RelationSelect),
      componentProps: {
        ...UOM_RELATION_PROPS,
        dialogTitle: '选择目标计量单位',
        placeholder: '请选择目标计量单位',
      },
    },
    {
      fieldName: 'ratio',
      label: '换算比例',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入换算比例',
        step: 0.01,
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
export function useGridColumns(): VxeTableGridOptions<MdmUomConvApi.UomConv>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'fromUomCode', title: '来源单位编码', minWidth: 140 },
    { field: 'fromUomName', title: '来源单位名称', minWidth: 160 },
    { field: 'toUomCode', title: '目标单位编码', minWidth: 140 },
    { field: 'toUomName', title: '目标单位名称', minWidth: 160 },
    {
      field: 'ratio',
      title: '换算比例',
      titlePrefix: {
        content:
          '1个来源单位 × 换算比例 = 目标单位数量，例如 kg 换算为 件 时填 2，表示 1kg = 2件',
      },
      minWidth: 120,
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
