import { markRaw } from 'vue';
import { CodeInput } from '#/components/code-input';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmResApi } from '#/api/mdm/res';

import { MDM_RES_TYPE_OPTIONS, MDM_STATUS_OPTIONS } from '../constants';

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
      fieldName: 'wcId',
      label: '工作中心编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'resType',
      label: '资源类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: MDM_RES_TYPE_OPTIONS,
        placeholder: '请选择资源类型',
      },
    },
    {
      fieldName: 'code',
      label: '编码',
      component: markRaw(CodeInput),
      componentProps: { ruleCode: 'MDM_RES_CODE',
        placeholder: '请输入编码',
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: MDM_STATUS_OPTIONS,
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

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'wcId',
      label: '工作中心编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'resType',
      label: '资源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: MDM_RES_TYPE_OPTIONS,
        placeholder: '请选择资源类型',
      },
    },
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
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: MDM_STATUS_OPTIONS,
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<MdmResApi.Res>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '主键',
      minWidth: 120,
    },
    {
      field: 'wcId',
      title: '工作中心编号',
      minWidth: 120,
    },
    {
      field: 'resType',
      title: '资源类型',
      minWidth: 120,
    },
    {
      field: 'code',
      title: '编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
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

