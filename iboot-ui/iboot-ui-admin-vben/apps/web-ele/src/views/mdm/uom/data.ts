import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

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
      label: '单位编码',
      component: 'Input',
      componentProps: { placeholder: '请输入单位编码' },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '单位名称',
      component: 'Input',
      componentProps: { placeholder: '请输入单位名称' },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'RadioGroup',
      componentProps: { options: STATUS_OPTIONS },
      defaultValue: 0,
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: { rows: 3, placeholder: '请输入备注' },
    },
  ];
}

export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '单位编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入单位编码' },
    },
    {
      fieldName: 'name',
      label: '单位名称',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入单位名称' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: STATUS_OPTIONS,
        placeholder: '请选择状态',
      },
    },
  ];
}

export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'code', title: '单位编码', minWidth: 150 },
    { field: 'name', title: '单位名称', minWidth: 150 },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      formatter: ({ cellValue }) => STATUS_OPTIONS[cellValue]?.label ?? '',
    },
    { field: 'remark', title: '备注', minWidth: 180 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
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
