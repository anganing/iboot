import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsShortageApi } from '#/api/aps/shortage';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS, PROJECT_RELATION_PROPS } from '#/views/mdm/relation-config';

import { DATETIME_PROPS } from '../form-props';
import { APS_VERSION_RELATION_PROPS } from '../relation-config';

/** 缺料处理表单：数量与状态由库存投影重算，人工只补供给预期和备注。 */
export function useHandleFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: { disabled: true },
    },
    {
      fieldName: 'matName',
      label: '物料名称',
      component: 'Input',
      componentProps: { disabled: true },
    },
    {
      fieldName: 'shortQty',
      label: '短缺数量',
      component: 'InputNumber',
      componentProps: { class: '!w-full', disabled: true },
    },
    {
      fieldName: 'expectedSupplyDate',
      label: '预计供给时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择预计供给时间' },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { maxLength: 500, placeholder: '请输入处理说明', rows: 2 },
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'versionId',
      label: '排程版本',
      component: markRaw(RelationSelect),
      componentProps: APS_VERSION_RELATION_PROPS,
    },
    {
      fieldName: 'projectId',
      label: '项目',
      component: markRaw(RelationSelect),
      componentProps: PROJECT_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '短缺物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入物料编码' },
    },
    {
      fieldName: 'matName',
      label: '物料名称',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入物料名称' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_SHORTAGE_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'needDate',
      label: '需求到位时间',
      component: 'RangePicker',
      componentProps: { ...getRangePickerDefaultProps(), clearable: true },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<ApsShortageApi.Shortage>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'scenarioCode', title: '场景编码', minWidth: 140 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 150 },
    { field: 'projectName', title: '项目', minWidth: 130 },
    { field: 'projectTaskName', title: '项目任务', minWidth: 140 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    { field: 'reqQty', title: '需求数量', minWidth: 110 },
    { field: 'availQty', title: '可用数量', minWidth: 110 },
    { field: 'shortQty', title: '短缺数量', minWidth: 110 },
    { field: 'uomCode', title: '单位', minWidth: 80 },
    {
      field: 'needDate',
      title: '需求到位时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'expectedSupplyDate',
      title: '预计供给时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_SHORTAGE_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 160 },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
