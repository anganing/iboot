import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsContainerApi } from '#/api/wms/containermgr';

import { markRaw } from 'vue';

import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { INV_ORG_RELATION_PROPS } from '#/views/mdm/relation-config';

import {
  CONTAINER_TYPE_RELATION_PROPS,
  LOC_RELATION_PROPS,
  WH_RELATION_PROPS,
} from '../relation-config';

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
      fieldName: 'containerNo',
      label: '容器编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入容器编号或条码',
      },
    },
    {
      fieldName: 'typeId',
      label: '容器类型',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: CONTAINER_TYPE_RELATION_PROPS,
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '所在仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '所在库位',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'length',
      label: '实际长度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'width',
      label: '实际宽度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'height',
      label: '实际高度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'grossWeight',
      label: '实际毛重',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位千克' },
    },
    {
      fieldName: 'status',
      label: '容器状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_container_status', 'string'),
        placeholder: '请选择容器状态',
      },
      defaultValue: 'EMPTY',
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
      label: '容器数据',
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
      fieldName: 'containerNo',
      label: '容器编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器编号',
      },
    },
    {
      fieldName: 'typeId',
      label: '容器类型',
      component: markRaw(RelationSelect),
      componentProps: CONTAINER_TYPE_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '所在仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '所在库位',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '容器状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_container_status', 'string'),
        placeholder: '请选择容器状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsContainerApi.Container>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'containerNo', title: '容器编号', minWidth: 160 },
    { field: 'typeName', title: '容器类型', minWidth: 140 },
    { field: 'whName', title: '所在仓库', minWidth: 140 },
    { field: 'locCode', title: '所在库位', minWidth: 140 },
    {
      field: 'size',
      title: '实际尺寸（长×宽×高）',
      minWidth: 180,
      formatter: ({ row }) =>
        [row.length, row.width, row.height].every(
          (item) => item !== null && item !== undefined,
        )
          ? `${row.length} × ${row.width} × ${row.height}`
          : '-',
    },
    { field: 'grossWeight', title: '实际毛重', minWidth: 110 },
    {
      field: 'status',
      title: '容器状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_container_status' } },
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
