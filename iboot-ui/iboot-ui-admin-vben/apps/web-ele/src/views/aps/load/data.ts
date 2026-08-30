import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsLoadApi } from '#/api/aps/load';

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
      fieldName: 'versionId',
      label: '排程版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产资源',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源编码',
      },
    },
    {
      fieldName: 'resName',
      label: '资源名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源名称',
      },
    },
    {
      fieldName: 'workDate',
      label: '负荷日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'loadMinutes',
      label: '计划负荷(分钟)',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计划负荷(分钟)',
      },
    },
    {
      fieldName: 'capacityMinutes',
      label: '可用产能(分钟)',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入可用产能(分钟)',
      },
    },
    {
      fieldName: 'loadRate',
      label: '负荷率',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入负荷率',
      },
    },
    {
      fieldName: 'overloadMinutes',
      label: '超负荷(分钟)',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入超负荷(分钟)',
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
      label: '资源负荷数据',
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
      fieldName: 'versionId',
      label: '排程版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产资源',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源编码',
      },
    },
    {
      fieldName: 'resName',
      label: '资源名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源名称',
      },
    },
    {
      fieldName: 'workDate',
      label: '负荷日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'loadMinutes',
      label: '计划负荷(分钟)',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计划负荷(分钟)',
      },
    },
    {
      fieldName: 'capacityMinutes',
      label: '可用产能(分钟)',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入可用产能(分钟)',
      },
    },
    {
      fieldName: 'loadRate',
      label: '负荷率',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入负荷率',
      },
    },
    {
      fieldName: 'overloadMinutes',
      label: '超负荷(分钟)',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入超负荷(分钟)',
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
export function useGridColumns(): VxeTableGridOptions<ApsLoadApi.Load>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'resId',
      title: '生产资源',
      minWidth: 120,
    },
    {
      field: 'resCode',
      title: '资源编码',
      minWidth: 120,
    },
    {
      field: 'resName',
      title: '资源名称',
      minWidth: 120,
    },
    {
      field: 'workDate',
      title: '负荷日期',
      minWidth: 120,
    },
    {
      field: 'loadMinutes',
      title: '计划负荷(分钟)',
      minWidth: 120,
    },
    {
      field: 'capacityMinutes',
      title: '可用产能(分钟)',
      minWidth: 120,
    },
    {
      field: 'loadRate',
      title: '负荷率',
      minWidth: 120,
    },
    {
      field: 'overloadMinutes',
      title: '超负荷(分钟)',
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
      field: 'versionVersion_no',
      title: '排程版本号',
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