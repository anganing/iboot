import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWaveApi } from '#/api/wms/wave';

import { markRaw } from 'vue';

import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import {
  OUT_ORDER_RELATION_PROPS,
  WH_RELATION_PROPS,
} from '../relation-config';

/** 时间选择器的公共配置 */
const DATETIME_PROPS = {
  class: '!w-full',
  format: 'YYYY-MM-DD HH:mm:ss',
  showTime: true,
  valueFormat: 'x',
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
      fieldName: 'waveNo',
      label: '波次编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入波次编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'waveType',
      label: '波次类型',
      component: 'Input',
      componentProps: {
        maxLength: 32,
        placeholder: '例如按订单、按区域',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划开始',
      component: 'DatePicker',
      componentProps: {
        ...DATETIME_PROPS,
        placeholder: '请选择计划开始时间',
      },
    },
    {
      fieldName: 'planEndTime',
      label: '计划结束',
      component: 'DatePicker',
      componentProps: {
        ...DATETIME_PROPS,
        placeholder: '请选择计划结束时间',
      },
    },
    {
      fieldName: 'status',
      label: '波次状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_wave_status', 'string'),
        placeholder: '请选择波次状态',
      },
      defaultValue: 'DRAFT',
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
      label: '出库波次数据',
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
      fieldName: 'waveNo',
      label: '波次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入波次编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'waveType',
      label: '波次类型',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入波次类型',
      },
    },
    {
      fieldName: 'status',
      label: '波次状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_wave_status', 'string'),
        placeholder: '请选择波次状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsWaveApi.Wave>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'waveNo', title: '波次编号', minWidth: 160 },
    { field: 'whName', title: '仓库', minWidth: 140 },
    { field: 'waveType', title: '波次类型', minWidth: 120 },
    {
      field: 'planStartTime',
      title: '计划开始',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'planEndTime',
      title: '计划结束',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '波次状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_wave_status' } },
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

// ==================== 子表（波次明细） ====================

/** 新增/修改的表单 */
export function useWaveLineFormSchema(): VbenFormSchema[] {
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
      fieldName: 'lineNo',
      label: '行号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入行号',
      },
    },
    {
      fieldName: 'outOrderId',
      label: '出库单',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OUT_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'outOrderLineId',
      label: '出库明细编号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入出库单明细编号',
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
      label: '明细状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_wave_status', 'string'),
        placeholder: '请选择明细状态',
      },
      defaultValue: 'DRAFT',
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

/** 列表的搜索表单 */
export function useWaveLineGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'outOrderId',
      label: '出库单',
      component: markRaw(RelationSelect),
      componentProps: OUT_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '明细状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_wave_status', 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/** 列表的字段 */
export function useWaveLineGridColumns(): VxeTableGridOptions<WmsWaveApi.WaveLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'outOrderNo', title: '出库单号', minWidth: 160 },
    { field: 'outOrderLineNo', title: '出库行号', minWidth: 100 },
    { field: 'priority', title: '优先级', minWidth: 90 },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_wave_status' } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
