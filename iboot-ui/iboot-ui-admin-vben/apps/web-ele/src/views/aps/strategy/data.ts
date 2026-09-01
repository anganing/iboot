import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsStrategyApi } from '#/api/aps/strategy';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

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
      label: '策略编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则分配',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '策略版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '例如 V1.0',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.APS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
    },
    {
      fieldName: 'objective',
      label: '优化目标',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        maxLength: 255,
        placeholder: '例如：最小化拖期，其次最小化换型时间',
        rows: 2,
      },
      formItemClass: 'col-span-2',
    },
    {
      fieldName: 'configJson',
      label: '约束权重配置',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        placeholder: '按 JSON 填写硬约束、软约束与权重',
        rows: 6,
      },
      formItemClass: 'col-span-2',
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
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '策略编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '策略版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<ApsStrategyApi.Strategy>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '策略编码', minWidth: 150 },
    { field: 'name', title: '策略名称', minWidth: 160 },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    { field: 'objective', title: '优化目标', minWidth: 220 },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_STRATEGY_STATUS },
      },
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
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
