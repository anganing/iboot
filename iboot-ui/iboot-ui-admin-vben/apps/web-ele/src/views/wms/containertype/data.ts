import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsContainerTypeApi } from '#/api/wms/containertype';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 尺寸类数值控件的公共配置 */
const DIMENSION_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  min: 0,
  precision: 2,
};

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
      label: '类型编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入容器类型编码',
      },
    },
    {
      fieldName: 'name',
      label: '类型名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入容器类型名称',
      },
    },
    {
      fieldName: 'stdLength',
      label: '标准长度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'stdWidth',
      label: '标准宽度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'stdHeight',
      label: '标准高度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'maxWeight',
      label: '最大承重',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位千克' },
    },
    {
      fieldName: 'maxVolume',
      label: '最大容积',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位立方米' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
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
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '容器类型数据',
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
      label: '类型编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器类型编码',
      },
    },
    {
      fieldName: 'name',
      label: '类型名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器类型名称',
      },
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
    { field: 'code', title: '类型编码', minWidth: 140 },
    { field: 'name', title: '类型名称', minWidth: 160 },
    {
      field: 'stdSize',
      title: '标准尺寸（长×宽×高）',
      minWidth: 180,
      formatter: ({ row }) =>
        [row.stdLength, row.stdWidth, row.stdHeight].every(
          (item) => item !== null && item !== undefined,
        )
          ? `${row.stdLength} × ${row.stdWidth} × ${row.stdHeight}`
          : '-',
    },
    { field: 'maxWeight', title: '最大承重', minWidth: 110 },
    { field: 'maxVolume', title: '最大容积', minWidth: 110 },
    {
      field: 'status',
      title: '状态',
      minWidth: 90,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
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
