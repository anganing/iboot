import type { VbenFormSchema } from '#/adapter/form';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { MAT_RELATION_PROPS, UOM_RELATION_PROPS } from '../relation-config';

export function useRouteOpMatFormSchema(): VbenFormSchema[] {
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
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0.000001,
        placeholder: '请输入数量',
        precision: 6,
      },
    },
    {
      fieldName: 'scrapRate',
      label: '损耗率',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        max: 1,
        precision: 6,
        placeholder: '0-1 之间，需求量按 qty ÷ (1 - 损耗率) 计算',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'issueMethod',
      label: '供料方式',
      component: 'Select',
      componentProps: {
        options: getDictOptions('mdm_issue_method', 'string'),
        placeholder: '请选择供料方式',
      },
      defaultValue: 'PICK',
    },
    {
      fieldName: 'mustIssueBeforeStart',
      label: '开工前必须发料',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: false,
    },
    {
      fieldName: 'uomId',
      label: '单位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: UOM_RELATION_PROPS,
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: { placeholder: '请输入备注' },
    },
  ];
}
