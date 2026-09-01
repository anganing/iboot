import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcResultApi } from '#/api/mes/qcresult';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import { DATETIME_PROPS, INT_PROPS, QTY_PROPS } from '../form-props';
import {
  QC_ITEM_RELATION_PROPS,
  QC_TASK_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 检验项编码与名称是快照，由后端从检验项目带出；结论与是否合格必须一致，由后端校验。
 */
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
      fieldName: 'taskId',
      label: '质检任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'itemId',
      label: '检验项目',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_ITEM_RELATION_PROPS,
    },
    {
      fieldName: 'sampleNo',
      label: '样本序号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        ...INT_PROPS,
        min: 1,
        placeholder: '同一检验项内不能重复',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'actualValue',
      label: '实测值',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 255,
        placeholder: '请输入实测值',
      },
    },
    {
      fieldName: 'conclusion',
      label: '项目结论',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_ITEM_CONCLUSION, 'string'),
        placeholder: '请选择项目结论',
      },
      defaultValue: 'PASS',
    },
    // 是否合格不作为独立字段：它与项目结论必须一致，由表单提交时按结论推导，
    // 让用户各选一次只会出现互相矛盾的取值，后端也会拦下。
    {
      fieldName: 'qtyQualified',
      label: '本项合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入本项合格数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'qtyRejected',
      label: '本项不合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入本项不合格数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'inspectorId',
      label: '检验员',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'inspectTime',
      label: '检验时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '留空取当前时间' },
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
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskId',
      label: '质检任务',
      component: markRaw(RelationSelect),
      componentProps: QC_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项编码',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项名称',
      },
    },
    {
      fieldName: 'inspectorId',
      label: '检验员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'passFlag',
      label: '是否合格',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否合格',
      },
    },
    {
      fieldName: 'conclusion',
      label: '项目结论',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_ITEM_CONCLUSION, 'string'),
        placeholder: '请选择项目结论',
      },
    },
    {
      fieldName: 'inspectTime',
      label: '检验时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesQcResultApi.QcResult>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 160 },
    { field: 'itemCode', title: '检验项编码', minWidth: 140 },
    { field: 'itemName', title: '检验项名称', minWidth: 170 },
    { field: 'sampleNo', title: '样本序号', minWidth: 100 },
    { field: 'actualValue', title: '实测值', minWidth: 130 },
    {
      field: 'passFlag',
      title: '是否合格',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'conclusion',
      title: '项目结论',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_QC_ITEM_CONCLUSION },
      },
    },
    { field: 'qtyQualified', title: '合格数量', minWidth: 110 },
    { field: 'qtyRejected', title: '不合格数量', minWidth: 110 },
    { field: 'inspectorName', title: '检验员', minWidth: 120 },
    {
      field: 'inspectTime',
      title: '检验时间',
      minWidth: 160,
      formatter: 'formatDateTime',
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
