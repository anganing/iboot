import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmDefectCodeApi } from '#/api/mdm/defectcode';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { MAT_CAT_RELATION_PROPS, OP_RELATION_PROPS } from '../relation-config';

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
      label: '缺陷编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '缺陷名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷名称',
        maxLength: 128,
      },
    },
    {
      fieldName: 'level',
      label: '缺陷等级',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('mdm_defect_level', 'string'),
        placeholder: '请选择缺陷等级',
      },
    },
    {
      fieldName: 'parentId',
      label: '父级缺陷',
      component: markRaw(RelationSelect),
      componentProps: {
        columns: [
          { field: 'code', label: '缺陷编码', minWidth: 140 },
          { field: 'name', label: '缺陷名称', minWidth: 160 },
          { field: 'level', label: '缺陷等级', minWidth: 100 },
        ],
        detailUri: '/mdm/defect-code/get?id={id}',
        dialogTitle: '选择父级缺陷',
        displayFields: ['code', 'name'],
        pageUri: '/mdm/defect-code/page',
        placeholder: '为空表示顶级',
      },
      help: '为空表示顶级',
    },
    {
      fieldName: 'opIds',
      label: '适用工序',
      component: markRaw(RelationSelect),
      componentProps: { ...OP_RELATION_PROPS, multiple: true },
      help: '为空表示不限工序',
    },
    {
      fieldName: 'matCatIds',
      label: '适用物料分类',
      component: markRaw(RelationSelect),
      componentProps: { ...MAT_CAT_RELATION_PROPS, multiple: true },
      help: '为空表示不限物料分类',
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
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '缺陷编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷编码',
      },
    },
    {
      fieldName: 'name',
      label: '缺陷名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷名称',
      },
    },
    {
      fieldName: 'level',
      label: '缺陷等级',
      component: 'Select',
      componentProps: {
        options: getDictOptions('mdm_defect_level', 'string'),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MdmDefectCodeApi.DefectCode>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '缺陷编码', minWidth: 120 },
    { field: 'name', title: '缺陷名称', minWidth: 160 },
    {
      field: 'level',
      title: '缺陷等级',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: 'mdm_defect_level' },
      },
    },
    { field: 'parentName', title: '父级缺陷', minWidth: 120 },
    {
      field: 'opNames',
      title: '适用工序',
      minWidth: 160,
      formatter: ({ cellValue }) => cellValue || '不限',
    },
    {
      field: 'matCatNames',
      title: '适用物料分类',
      minWidth: 160,
      formatter: ({ cellValue }) => cellValue || '不限',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 120 },
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
