import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import {
  MAT_RELATION_PROPS,
  OP_RELATION_PROPS,
  WC_RELATION_PROPS,
} from '../relation-config';

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
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
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
    { type: 'seq', title: '序号', width: 60 },
    { field: 'matCode', title: '物料编码', minWidth: 150 },
    { field: 'matName', title: '物料名称', minWidth: 180 },
    { field: 'version', title: '版本号', minWidth: 100 },
    {
      field: 'status',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    { field: 'effectiveDate', title: '生效日期', minWidth: 120 },
    { field: 'expiryDate', title: '失效日期', minWidth: 120 },
    { field: 'remark', title: '备注', minWidth: 160 },
    {
      title: '操作',
      width: 340,
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
      fieldName: 'overrideSetupMinutes',
      label: '覆盖准备工时（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '为空继承标准工序',
      },
    },
    {
      fieldName: 'overrideStdMinutesPerUnit',
      label: '覆盖单件工时（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 6,
        placeholder: '为空继承标准工序',
      },
    },
    {
      fieldName: 'overrideQueueMinutes',
      label: '覆盖排队等待（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '为空继承标准工序',
      },
    },
    {
      fieldName: 'overrideMoveMinutes',
      label: '覆盖转移时间（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '为空继承标准工序',
      },
    },
    {
      fieldName: 'keyFlag',
      label: '关键工序',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
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
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'skipPolicy',
      label: '跳序策略',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_POLICY, 'string'),
        placeholder: '为空按不允许跳序处理',
        clearable: true,
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
