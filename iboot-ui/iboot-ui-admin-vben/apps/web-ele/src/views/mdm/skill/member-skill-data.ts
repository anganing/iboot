import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { USER_RELATION_PROPS } from '../relation-config';

/** 人员技能新增/修改的表单 */
export function useMemberSkillFormSchema(): VbenFormSchema[] {
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
      fieldName: 'userId',
      label: '人员',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'level',
      label: '技能等级',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入技能等级',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'effectiveDate',
      label: '生效日期',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        placeholder: '请选择生效日期',
        type: 'date',
        valueFormat: 'YYYY-MM-DD',
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        placeholder: '为空表示长期有效',
        type: 'date',
        valueFormat: 'YYYY-MM-DD',
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
  ];
}

/** 人员技能列表的字段 */
export function useMemberSkillGridColumns(): VxeTableGridOptions<MdmSkillApi.MemberSkill>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'userName', title: '人员', minWidth: 140 },
    { field: 'level', title: '技能等级', minWidth: 100 },
    { field: 'effectiveDate', title: '生效日期', minWidth: 120 },
    { field: 'expiryDate', title: '失效日期', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
