import type { VbenFormSchema } from '#/adapter/form';

import { markRaw } from 'vue';

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
