import type { VbenFormSchema } from '#/adapter/form';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';

import { TOOL_RELATION_PROPS } from '../relation-config';

export function useRouteOpToolFormSchema(): VbenFormSchema[] {
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
      fieldName: 'toolId',
      label: '工装',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: TOOL_RELATION_PROPS,
    },
    {
      fieldName: 'qty',
      label: '需求数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入需求数量',
      },
      defaultValue: 1,
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必备',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      defaultValue: true,
    },
  ];
}
