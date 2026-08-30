import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcPlanApi } from '#/api/mes/qcplan';

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
      label: '检验方案编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验方案编码',
      },
    },
    {
      fieldName: 'name',
      label: '检验方案名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验方案名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '方案版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入方案版本号',
      },
    },
    {
      fieldName: 'sampleType',
      label: '抽样类型：FULL、FIXED_QTY、PERCENT、AQL',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择抽样类型：FULL、FIXED_QTY、PERCENT、AQL',
      },
    },
    {
      fieldName: 'sampleValue',
      label: '固定数量或抽样比例',
      component: 'Input',
      componentProps: {
        placeholder: '请输入固定数量或抽样比例',
      },
    },
    {
      fieldName: 'aql',
      label: 'AQL接收质量限',
      component: 'Input',
      componentProps: {
        placeholder: '请输入AQL接收质量限',
      },
    },
    {
      fieldName: 'acceptanceRule',
      label: '接收判定规则',
      component: 'Input',
      componentProps: {
        placeholder: '请输入接收判定规则',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、PUBLISHED、DISABLED',
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
      label: 'MES-检验方案数据',
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
      label: '检验方案编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验方案编码',
      },
    },
    {
      fieldName: 'name',
      label: '检验方案名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验方案名称',
      },
    },
    {
      fieldName: 'versionNo',
      label: '方案版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入方案版本号',
      },
    },
    {
      fieldName: 'sampleType',
      label: '抽样类型：FULL、FIXED_QTY、PERCENT、AQL',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择抽样类型：FULL、FIXED_QTY、PERCENT、AQL',
      },
    },
    {
      fieldName: 'sampleValue',
      label: '固定数量或抽样比例',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入固定数量或抽样比例',
      },
    },
    {
      fieldName: 'aql',
      label: 'AQL接收质量限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入AQL接收质量限',
      },
    },
    {
      fieldName: 'acceptanceRule',
      label: '接收判定规则',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入接收判定规则',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、PUBLISHED、DISABLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、PUBLISHED、DISABLED',
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
export function useGridColumns(): VxeTableGridOptions<MesQcPlanApi.QcPlan>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '检验方案编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '检验方案名称',
      minWidth: 120,
    },
    {
      field: 'versionNo',
      title: '方案版本号',
      minWidth: 120,
    },
    {
      field: 'sampleType',
      title: '抽样类型：FULL、FIXED_QTY、PERCENT、AQL',
      minWidth: 120,
    },
    {
      field: 'sampleValue',
      title: '固定数量或抽样比例',
      minWidth: 120,
    },
    {
      field: 'aql',
      title: 'AQL接收质量限',
      minWidth: 120,
    },
    {
      field: 'acceptanceRule',
      title: '接收判定规则',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、PUBLISHED、DISABLED',
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