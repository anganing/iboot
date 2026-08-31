import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsLocApi } from '#/api/wms/loc';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { WH_RELATION_PROPS, ZONE_RELATION_PROPS } from '../relation-config';

/** 尺寸类数值控件的公共配置，单位毫米 */
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
      label: '库位编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入库位编码',
      },
    },
    {
      fieldName: 'whId',
      label: '所属仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'zoneId',
      label: '所属库区',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: ZONE_RELATION_PROPS,
    },
    {
      fieldName: 'locType',
      label: '库位类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_LOC_TYPE, 'string'),
        placeholder: '请选择库位类型',
      },
    },
    {
      fieldName: 'aisleNo',
      label: '巷道号',
      component: 'Input',
      componentProps: { maxLength: 32, placeholder: '请输入巷道号' },
    },
    {
      fieldName: 'rowNo',
      label: '排号',
      component: 'Input',
      componentProps: { maxLength: 32, placeholder: '请输入排号' },
    },
    {
      fieldName: 'colNo',
      label: '列号',
      component: 'Input',
      componentProps: { maxLength: 32, placeholder: '请输入列号' },
    },
    {
      fieldName: 'levelNo',
      label: '层号',
      component: 'Input',
      componentProps: { maxLength: 32, placeholder: '请输入层号' },
    },
    {
      fieldName: 'depthNo',
      label: '纵深号',
      component: 'InputNumber',
      help: '双深货架专用：1 为外侧货位，靠巷道、靠近堆垛机，可直接存取；2 为里侧货位，远离堆垛机，需先移走外侧货物。单深货位留空',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '双深货位填 1 或 2',
      },
    },
    {
      fieldName: 'maxLength',
      label: '最大长度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'maxWidth',
      label: '最大宽度',
      component: 'InputNumber',
      componentProps: { ...DIMENSION_PROPS, placeholder: '单位毫米' },
    },
    {
      fieldName: 'maxHeight',
      label: '最大高度',
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
      fieldName: 'maxContainerQty',
      label: '最大容器数',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '可存放的容器数量',
      },
    },
    {
      fieldName: 'dimensionCheckMode',
      label: '尺寸校验',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_DIMENSION_CHECK_MODE, 'string'),
        placeholder: '请选择尺寸校验模式',
      },
      defaultValue: 'NONE',
    },
    {
      fieldName: 'status',
      label: '库位状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_LOC_STATUS, 'string'),
        placeholder: '请选择库位状态',
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
      label: '库位数据',
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
      label: '库位编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位编码',
      },
    },
    {
      fieldName: 'whId',
      label: '所属仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'zoneId',
      label: '所属库区',
      component: markRaw(RelationSelect),
      componentProps: ZONE_RELATION_PROPS,
    },
    {
      fieldName: 'locType',
      label: '库位类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_LOC_TYPE, 'string'),
        placeholder: '请选择库位类型',
      },
    },
    {
      fieldName: 'aisleNo',
      label: '巷道号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入巷道号' },
    },
    {
      fieldName: 'status',
      label: '库位状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_LOC_STATUS, 'string'),
        placeholder: '请选择库位状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsLocApi.Loc>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '库位编码', minWidth: 150 },
    { field: 'whName', title: '所属仓库', minWidth: 140 },
    { field: 'zoneName', title: '所属库区', minWidth: 140 },
    {
      field: 'locType',
      title: '库位类型',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_LOC_TYPE } },
    },
    {
      field: 'position',
      title: '巷道/排/列/层',
      minWidth: 140,
      formatter: ({ row }) =>
        [row.aisleNo, row.rowNo, row.colNo, row.levelNo]
          .filter(Boolean)
          .join('-') || '-',
    },
    {
      field: 'depthNo',
      title: '纵深号',
      titlePrefix: {
        content:
          '双深货架的存取深度：1 为外侧货位，靠巷道、靠近堆垛机，可直接存取；2 为里侧货位，远离堆垛机，需先移走外侧货物。单深货位为空',
      },
      minWidth: 90,
      formatter: ({ row }) => row.depthNo ?? '-',
    },
    { field: 'maxWeight', title: '最大承重', minWidth: 110 },
    { field: 'maxContainerQty', title: '最大容器数', minWidth: 110 },
    {
      field: 'dimensionCheckMode',
      title: '尺寸校验',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_DIMENSION_CHECK_MODE },
      },
    },
    {
      field: 'status',
      title: '库位状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_LOC_STATUS } },
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
