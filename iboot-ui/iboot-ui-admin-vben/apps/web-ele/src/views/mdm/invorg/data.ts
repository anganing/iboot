import { markRaw } from 'vue';
import { CodeInput } from '#/components/code-input';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmInvOrgApi } from '#/api/mdm/invorg';

const STATUS_OPTIONS = [
  { label: '启用', value: 0 },
  { label: '停用', value: 1 },
];

export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: { triggerFields: [''], show: () => false },
    },
    {
      fieldName: 'code',
      label: '编码',
      component: markRaw(CodeInput),
      componentProps: { ruleCode: 'MDM_INV_ORG_CODE', placeholder: '请输入编码' },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: { placeholder: '请输入名称' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      defaultValue: 0,
      componentProps: { options: STATUS_OPTIONS, buttonStyle: 'solid', optionType: 'button' },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { placeholder: '请输入备注', rows: 3 },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    { fieldName: 'code', label: '编码', component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入编码' } },
    { fieldName: 'name', label: '名称', component: 'Input', componentProps: { clearable: true, placeholder: '请输入名称' } },
    { fieldName: 'status', label: '状态', component: 'Select', componentProps: { clearable: true, options: STATUS_OPTIONS, placeholder: '请选择状态' } },
  ];
}

export function useGridColumns(): VxeTableGridOptions<MdmInvOrgApi.InvOrg>['columns'] {
  return [
    { field: 'id', title: '编号', minWidth: 120 },
    { field: 'code', title: '编码', minWidth: 140 },
    { field: 'name', title: '名称', minWidth: 180 },
    { field: 'status', title: '状态', minWidth: 100, formatter: ({ cellValue }: { cellValue: number }) => STATUS_OPTIONS.find((item) => item.value === cellValue)?.label ?? '' },
    { field: 'remark', title: '备注', minWidth: 180 },
    { field: 'createTime', title: '创建时间', minWidth: 180, formatter: 'formatDateTime' },
    { title: '操作', width: 160, fixed: 'right', slots: { default: 'actions' } },
  ];
}
