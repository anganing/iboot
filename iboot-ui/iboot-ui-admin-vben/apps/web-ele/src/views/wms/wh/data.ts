import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWhApi } from '#/api/wms/wh';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  PLANT_RELATION_PROPS,
} from '#/views/mdm/relation-config';

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
      label: '仓库编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入仓库编码',
      },
    },
    {
      fieldName: 'name',
      label: '仓库名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入仓库名称',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '所属工厂',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'whType',
      label: '仓库类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_wh_type', 'string'),
        placeholder: '请选择仓库类型',
      },
    },
    {
      fieldName: 'managedBy',
      label: '管理方式',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_managed_by', 'string'),
        placeholder: '请选择管理方式',
      },
      defaultValue: 'MANUAL',
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部系统编码',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '对接 WCS/AGV 时填写',
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
      label: '仓库数据',
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
      label: '仓库编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编码',
      },
    },
    {
      fieldName: 'name',
      label: '仓库名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库名称',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'plantId',
      label: '所属工厂',
      component: markRaw(RelationSelect),
      componentProps: PLANT_RELATION_PROPS,
    },
    {
      fieldName: 'whType',
      label: '仓库类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_wh_type', 'string'),
        placeholder: '请选择仓库类型',
      },
    },
    {
      fieldName: 'managedBy',
      label: '管理方式',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_managed_by', 'string'),
        placeholder: '请选择管理方式',
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
export function useGridColumns(): VxeTableGridOptions<WmsWhApi.Wh>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '仓库编码', minWidth: 140 },
    { field: 'name', title: '仓库名称', minWidth: 160 },
    { field: 'invOrgName', title: '库存组织', minWidth: 140 },
    { field: 'plantName', title: '所属工厂', minWidth: 140 },
    {
      field: 'whType',
      title: '仓库类型',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_wh_type' } },
    },
    {
      field: 'managedBy',
      title: '管理方式',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_managed_by' } },
    },
    { field: 'externalSystemCode', title: '外部系统编码', minWidth: 140 },
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
