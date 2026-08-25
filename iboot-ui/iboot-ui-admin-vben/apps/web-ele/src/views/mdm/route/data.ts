import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';

import { markRaw } from 'vue';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { MDM_BOOLEAN_OPTIONS } from '../constants';
import {
  MAT_RELATION_PROPS,
  OP_RELATION_PROPS,
  WC_RELATION_PROPS,
} from '../relation-config';

const versionStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
];

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
      fieldName: 'version',
      label: '版本号',
      rules: 'required',
      component: 'Input',
      componentProps: { placeholder: '请输入版本号' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: versionStatusOptions,
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'effectiveDate',
      label: '生效日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
        class: '!w-full',
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'DatePicker',
      componentProps: {
        format: 'YYYY-MM-DD',
        valueFormat: 'YYYY-MM-DD',
        class: '!w-full',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: { placeholder: '请输入备注' },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'version',
      label: '版本号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入版本号' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: versionStatusOptions,
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'effectiveDate',
      label: '生效日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions<MdmRouteApi.Route>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { field: 'matCode', title: '物料编码', minWidth: 150 },
    { field: 'matName', title: '物料名称', minWidth: 180 },
    { field: 'version', title: '版本号', minWidth: 100 },
    {
      field: 'status',
      title: '状态',
      width: 100,
      slots: { default: 'status' },
    },
    { field: 'effectiveDate', title: '生效日期', minWidth: 120 },
    { field: 'expiryDate', title: '失效日期', minWidth: 120 },
    { field: 'remark', title: '备注', minWidth: 160 },
    {
      title: '操作',
      width: 260,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

export function useRouteOpFormSchema(): VbenFormSchema[] {
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
      fieldName: 'opId',
      label: '标准工序',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_RELATION_PROPS,
    },
    {
      fieldName: 'wcId',
      label: '工作中心',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WC_RELATION_PROPS,
    },
    {
      fieldName: 'parallelGroup',
      label: '并行组',
      component: 'Input',
      componentProps: { placeholder: '请输入并行组' },
    },
    {
      fieldName: 'keyFlag',
      label: '关键工序',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: MDM_BOOLEAN_OPTIONS,
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'faiFlag',
      label: '首件检验',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: MDM_BOOLEAN_OPTIONS,
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: { placeholder: '请输入备注' },
    },
  ];
}
