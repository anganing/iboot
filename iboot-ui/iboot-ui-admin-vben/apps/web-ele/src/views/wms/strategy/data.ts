import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsStrategyApi } from '#/api/wms/strategy';

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
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'strategyType',
      label: '策略类型：PUTAWAY、PICK、REPLENISH',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择策略类型：PUTAWAY、PICK、REPLENISH',
      },
    },
    {
      fieldName: 'whId',
      label: '适用仓库编号，0表示全局',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入适用仓库编号，0表示全局',
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
      fieldName: 'configJson',
      label: '策略条件与排序规则配置',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入策略条件与排序规则配置',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级，数值越大越优先',
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
      label: 'WMS-仓储策略数据',
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
      fieldName: 'strategyType',
      label: '策略类型：PUTAWAY、PICK、REPLENISH',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择策略类型：PUTAWAY、PICK、REPLENISH',
      },
    },
    {
      fieldName: 'whId',
      label: '适用仓库编号，0表示全局',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入适用仓库编号，0表示全局',
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
      fieldName: 'configJson',
      label: '策略条件与排序规则配置',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入策略条件与排序规则配置',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级，数值越大越优先',
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
export function useGridColumns(): VxeTableGridOptions<WmsStrategyApi.Strategy>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '策略编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '策略名称',
      minWidth: 120,
    },
    {
      field: 'strategyType',
      title: '策略类型：PUTAWAY、PICK、REPLENISH',
      minWidth: 120,
    },
    {
      field: 'versionNo',
      title: '策略版本号',
      minWidth: 120,
    },
    {
      field: 'configJson',
      title: '策略条件与排序规则配置',
      minWidth: 120,
    },
    {
      field: 'priority',
      title: '优先级，数值越大越优先',
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
      field: 'whCode',
      title: '适用仓库编号，0表示全局编码',
      minWidth: 120,
    },
    {
      field: 'whName',
      title: '适用仓库编号，0表示全局名称',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}