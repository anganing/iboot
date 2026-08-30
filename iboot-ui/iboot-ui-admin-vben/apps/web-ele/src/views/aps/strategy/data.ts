import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsStrategyApi } from '#/api/aps/strategy';

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
      label: '排程策略编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '排程策略名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程策略名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '策略版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入策略版本号',
      },
    },
    {
      fieldName: 'objective',
      label: '优化目标说明',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优化目标说明',
      },
    },
    {
      fieldName: 'configJson',
      label: '硬约束、软约束及权重配置',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入硬约束、软约束及权重配置',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、ENABLED、DISABLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'APS-排程策略数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '排程策略编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '排程策略名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程策略名称',
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
      fieldName: 'objective',
      label: '优化目标说明',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优化目标说明',
      },
    },
    {
      fieldName: 'configJson',
      label: '硬约束、软约束及权重配置',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入硬约束、软约束及权重配置',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、ENABLED、DISABLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、ENABLED、DISABLED',
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
export function useGridColumns(): VxeTableGridOptions<ApsStrategyApi.Strategy>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '排程策略编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '排程策略名称',
      minWidth: 120,
    },
    {
      field: 'versionNo',
      title: '策略版本号',
      minWidth: 120,
    },
    {
      field: 'objective',
      title: '优化目标说明',
      minWidth: 120,
    },
    {
      field: 'configJson',
      title: '硬约束、软约束及权重配置',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、ENABLED、DISABLED',
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
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}