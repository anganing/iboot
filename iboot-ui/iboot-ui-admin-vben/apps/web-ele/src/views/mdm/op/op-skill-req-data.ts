import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { SKILL_RELATION_PROPS } from '../relation-config';

/** 工序技能要求新增/修改的表单 */
export function useOpSkillReqFormSchema(): VbenFormSchema[] {
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
      fieldName: 'skillId',
      label: '技能',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: SKILL_RELATION_PROPS,
    },
    {
      fieldName: 'minLevel',
      label: '最低等级',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入最低技能等级',
      },
      defaultValue: 1,
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

/** 工序技能要求列表的字段 */
export function useOpSkillReqGridColumns(): VxeTableGridOptions<MdmSkillApi.OpSkillReq>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'skillCode', title: '技能编码', minWidth: 140 },
    { field: 'skillName', title: '技能名称', minWidth: 160 },
    { field: 'minLevel', title: '最低技能等级', minWidth: 120 },
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
      width: 150,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
