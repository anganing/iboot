import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsStrategyApi } from '#/api/wms/strategy';

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
      fieldName: 'code',
      label: '策略编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入策略编码',
      },
    },
    {
      fieldName: 'name',
      label: '策略名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入策略名称',
      },
    },
    {
      fieldName: 'strategyType',
      label: '策略类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_STRATEGY_TYPE, 'string'),
        placeholder: '请选择策略类型',
      },
    },
    {
      fieldName: 'whId',
      label: '适用仓库',
      component: markRaw(RelationSelect),
      componentProps: {
        ...WH_RELATION_PROPS,
        placeholder: '留空表示全局生效',
      },
    },
    {
      fieldName: 'versionNo',
      label: '版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '例如 V1.0',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '数值越大越优先',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '策略状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择策略状态',
      },
      defaultValue: 'DRAFT',
    },
    {
      fieldName: 'configJson',
      label: '策略配置',
      component: 'Textarea',
      componentProps: {
        placeholder: '按 JSON 填写条件与排序规则',
        rows: 6,
      },
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
      label: '仓储策略数据',
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
      label: '策略类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_STRATEGY_TYPE, 'string'),
        placeholder: '请选择策略类型',
      },
    },
    {
      fieldName: 'whId',
      label: '适用仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '策略状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_STRATEGY_STATUS, 'string'),
        placeholder: '请选择策略状态',
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
    { field: 'code', title: '策略编码', minWidth: 140 },
    { field: 'name', title: '策略名称', minWidth: 160 },
    {
      field: 'strategyType',
      title: '策略类型',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_STRATEGY_TYPE } },
    },
    {
      field: 'whName',
      title: '适用仓库',
      minWidth: 140,
      formatter: ({ row }) => row.whName || '全局',
    },
    { field: 'versionNo', title: '版本号', minWidth: 100 },
    { field: 'priority', title: '优先级', minWidth: 90 },
    {
      field: 'status',
      title: '策略状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_STRATEGY_STATUS } },
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
