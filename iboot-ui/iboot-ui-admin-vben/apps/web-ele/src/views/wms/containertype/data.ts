import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsContainerTypeApi } from '#/api/wms/containertype';

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
      label: '容器类型编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器类型编码',
      },
    },
    {
      fieldName: 'name',
      label: '容器类型名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器类型名称',
      },
    },
    {
      fieldName: 'stdLength',
      label: '标准长度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标准长度，单位毫米',
      },
    },
    {
      fieldName: 'stdWidth',
      label: '标准宽度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标准宽度，单位毫米',
      },
    },
    {
      fieldName: 'stdHeight',
      label: '标准高度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标准高度，单位毫米',
      },
    },
    {
      fieldName: 'maxWeight',
      label: '最大承重',
      component: 'Input',
      componentProps: {
        placeholder: '请输入最大承重',
      },
    },
    {
      fieldName: 'maxVolume',
      label: '最大容积',
      component: 'Input',
      componentProps: {
        placeholder: '请输入最大容积',
      },
    },
    {
      fieldName: 'status',
      label: '状态：0-启用，1-停用',
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
      label: 'WMS-容器类型数据',
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
      label: '容器类型编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器类型编码',
      },
    },
    {
      fieldName: 'name',
      label: '容器类型名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器类型名称',
      },
    },
    {
      fieldName: 'stdLength',
      label: '标准长度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入标准长度，单位毫米',
      },
    },
    {
      fieldName: 'stdWidth',
      label: '标准宽度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入标准宽度，单位毫米',
      },
    },
    {
      fieldName: 'stdHeight',
      label: '标准高度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入标准高度，单位毫米',
      },
    },
    {
      fieldName: 'maxWeight',
      label: '最大承重',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入最大承重',
      },
    },
    {
      fieldName: 'maxVolume',
      label: '最大容积',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入最大容积',
      },
    },
    {
      fieldName: 'status',
      label: '状态：0-启用，1-停用',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：0-启用，1-停用',
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
export function useGridColumns(): VxeTableGridOptions<WmsContainerTypeApi.ContainerType>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '容器类型编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '容器类型名称',
      minWidth: 120,
    },
    {
      field: 'stdLength',
      title: '标准长度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'stdWidth',
      title: '标准宽度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'stdHeight',
      title: '标准高度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'maxWeight',
      title: '最大承重',
      minWidth: 120,
    },
    {
      field: 'maxVolume',
      title: '最大容积',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：0-启用，1-停用',
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