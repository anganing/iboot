import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDefectApi } from '#/api/mes/defect';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import { QTY_PROPS } from '../form-props';
import {
  DEFECT_CODE_RELATION_PROPS,
  QC_RESULT_RELATION_PROPS,
  QC_TASK_RELATION_PROPS,
} from '../relation-config';
import DefectFileField from './modules/defect-file-field.vue';

/**
 * 新增/修改的表单。
 *
 * 缺陷编码、名称、等级是 mdm 缺陷代码库的快照，前端只选缺陷代码；附件走独立子表，全量替换。
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
      fieldName: 'resultId',
      label: '关联检验结果',
      component: markRaw(RelationSelect),
      // 检验结果必须属于同一质检任务，否则缺陷追溯链是错的，后端也会校验归属
      dependencies: {
        triggerFields: ['taskId'],
        componentProps: (values) => ({
          ...QC_RESULT_RELATION_PROPS,
          disabled: !values.taskId,
          extraParams: { taskId: values.taskId },
          placeholder: values.taskId
            ? '未关联具体检验项可留空'
            : '请先选择质检任务',
        }),
      },
      defaultValue: 0,
    },
    {
      fieldName: 'defectCodeId',
      label: '缺陷代码',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: DEFECT_CODE_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '缺陷数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入缺陷数量' },
    },
    {
      fieldName: 'location',
      label: '缺陷位置',
      component: 'Input',
      componentProps: {
        maxLength: 255,
        placeholder: '如 A 面第三槽',
      },
    },
    {
      fieldName: 'description',
      label: '缺陷描述',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入缺陷描述',
        rows: 2,
      },
    },
    {
      fieldName: 'files',
      label: '缺陷附件',
      component: markRaw(DefectFileField),
      formItemClass: 'col-span-2',
      defaultValue: [],
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
      fieldName: 'defectCodeId',
      label: '缺陷代码',
      component: markRaw(RelationSelect),
      componentProps: DEFECT_CODE_RELATION_PROPS,
    },
    {
      fieldName: 'defectCode',
      label: '缺陷编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷编码',
      },
    },
    {
      fieldName: 'defectName',
      label: '缺陷名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷名称',
      },
    },
    {
      fieldName: 'defectLevel',
      label: '缺陷等级',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DEFECT_LEVEL, 'string'),
        placeholder: '请选择缺陷等级',
      },
    },
    {
      fieldName: 'location',
      label: '缺陷位置',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷位置',
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
export function useGridColumns(): VxeTableGridOptions<MesDefectApi.Defect>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '质检任务号', minWidth: 160 },
    { field: 'resultItemName', title: '关联检验项', minWidth: 150 },
    { field: 'defectCode', title: '缺陷编码', minWidth: 140 },
    { field: 'defectName', title: '缺陷名称', minWidth: 160 },
    {
      field: 'defectLevel',
      title: '缺陷等级',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DEFECT_LEVEL },
      },
    },
    { field: 'qty', title: '缺陷数量', minWidth: 110 },
    { field: 'location', title: '缺陷位置', minWidth: 140 },
    { field: 'description', title: '缺陷描述', minWidth: 180 },
    {
      field: 'fileCount',
      title: '附件',
      minWidth: 100,
      slots: { default: 'fileCount' },
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
