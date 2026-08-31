import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsZoneApi } from '#/api/wms/zone';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { WH_RELATION_PROPS } from '../relation-config';

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
      fieldName: 'whId',
      label: '所属仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'code',
      label: '库区编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入库区编码',
      },
    },
    {
      fieldName: 'name',
      label: '库区名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入库区名称',
      },
    },
    {
      fieldName: 'zoneType',
      label: '库区类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_ZONE_TYPE, 'string'),
        placeholder: '请选择库区类型',
      },
    },
    {
      fieldName: 'allowMixedMat',
      label: '混放物料',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: true,
    },
    {
      fieldName: 'allowMixedLot',
      label: '混放批次',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: true,
    },
    {
      fieldName: 'tempRange',
      label: '温区要求',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '例如 2~8℃，无要求可留空',
      },
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
      label: '库区数据',
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
      fieldName: 'whId',
      label: '所属仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'code',
      label: '库区编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区编码',
      },
    },
    {
      fieldName: 'name',
      label: '库区名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区名称',
      },
    },
    {
      fieldName: 'zoneType',
      label: '库区类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_ZONE_TYPE, 'string'),
        placeholder: '请选择库区类型',
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
export function useGridColumns(): VxeTableGridOptions<WmsZoneApi.Zone>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '库区编码', minWidth: 140 },
    { field: 'name', title: '库区名称', minWidth: 160 },
    { field: 'whCode', title: '仓库编码', minWidth: 130 },
    { field: 'whName', title: '仓库名称', minWidth: 150 },
    {
      field: 'zoneType',
      title: '库区类型',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ZONE_TYPE } },
    },
    {
      field: 'allowMixedMat',
      title: '混放物料',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'allowMixedLot',
      title: '混放批次',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'tempRange', title: '温区要求', minWidth: 120 },
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
