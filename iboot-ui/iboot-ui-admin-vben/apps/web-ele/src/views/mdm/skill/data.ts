import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmSkillApi } from '#/api/mdm/skill';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

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
      label: '技能编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '技能名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入技能名称',
        maxLength: 128,
      },
    },
    {
      fieldName: 'levelDesc',
      label: '等级体系说明',
      component: 'Textarea',
      componentProps: {
        placeholder: '如：1-见习 2-独立操作 3-可带教 4-专家',
        rows: 3,
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
      label: '技能编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入技能编码',
      },
    },
    {
      fieldName: 'name',
      label: '技能名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入技能名称',
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
export function useGridColumns(): VxeTableGridOptions<MdmSkillApi.Skill>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '技能编码', minWidth: 120 },
    { field: 'name', title: '技能名称', minWidth: 160 },
    { field: 'levelDesc', title: '等级体系说明', minWidth: 240 },
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
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
