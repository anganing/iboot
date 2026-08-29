import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

const STATUS_OPTIONS = [
  { label: '开启', value: 0 },
  { label: '关闭', value: 1 },
];

const RESET_CYCLE_OPTIONS = [
  { label: '不重置', value: 0 },
  { label: '按年', value: 1 },
  { label: '按月', value: 2 },
  { label: '按日', value: 3 },
];

/** 新增/修改编码规则的表单 */
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
      fieldName: 'ruleCode',
      label: '规则编码',
      component: 'Input',
      componentProps: { placeholder: '请输入规则编码' },
      rules: 'required',
    },
    {
      fieldName: 'name',
      label: '规则名称',
      component: 'Input',
      componentProps: { placeholder: '请输入规则名称' },
      rules: 'required',
    },
    {
      fieldName: 'prefix',
      label: '前缀',
      component: 'Input',
      componentProps: { placeholder: '请输入前缀' },
    },
    {
      fieldName: 'suffix',
      label: '后缀',
      component: 'Input',
      componentProps: { placeholder: '请输入后缀' },
    },
    {
      fieldName: 'yearEnabled',
      label: '包含年',
      component: 'Switch',
      componentProps: { checkedChildren: '是', unCheckedChildren: '否' },
      defaultValue: false,
    },
    {
      fieldName: 'monthEnabled',
      label: '包含月',
      component: 'Switch',
      componentProps: { checkedChildren: '是', unCheckedChildren: '否' },
      defaultValue: false,
    },
    {
      fieldName: 'dayEnabled',
      label: '包含日',
      component: 'Switch',
      componentProps: { checkedChildren: '是', unCheckedChildren: '否' },
      defaultValue: false,
    },
    {
      fieldName: 'serialLength',
      label: '流水号长度',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        max: 18,
        min: 1,
      },
      defaultValue: 4,
      rules: 'required',
    },
    {
      fieldName: 'serialStart',
      label: '起始值',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
      },
      defaultValue: 1,
      rules: 'required',
    },
    {
      fieldName: 'serialStep',
      label: '步长',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
      },
      defaultValue: 1,
      rules: 'required',
    },
    {
      fieldName: 'resetCycle',
      label: '重置周期',
      component: 'RadioGroup',
      componentProps: { options: RESET_CYCLE_OPTIONS },
      defaultValue: 0,
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
      componentProps: { placeholder: '请输入备注', rows: 3 },
    },
  ];
}

/** 编码规则的查询表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'ruleCode',
      label: '规则编码',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入规则编码' },
    },
    {
      fieldName: 'name',
      label: '规则名称',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入规则名称' },
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

/** 编码规则的表格字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'id', title: '编号', minWidth: 90 },
    { field: 'ruleCode', title: '规则编码', minWidth: 160 },
    { field: 'name', title: '规则名称', minWidth: 160 },
    { field: 'prefix', title: '前缀', minWidth: 110 },
    {
      field: 'yearEnabled',
      title: '年',
      minWidth: 60,
      formatter: ({ cellValue }) => (cellValue ? '是' : '否'),
    },
    {
      field: 'monthEnabled',
      title: '月',
      minWidth: 60,
      formatter: ({ cellValue }) => (cellValue ? '是' : '否'),
    },
    {
      field: 'dayEnabled',
      title: '日',
      minWidth: 60,
      formatter: ({ cellValue }) => (cellValue ? '是' : '否'),
    },
    { field: 'suffix', title: '后缀', minWidth: 110 },
    { field: 'serialLength', title: '流水长度', minWidth: 100 },
    { field: 'serialStart', title: '起始值', minWidth: 90 },
    { field: 'serialStep', title: '步长', minWidth: 80 },
    {
      field: 'resetCycle',
      title: '重置周期',
      minWidth: 100,
      formatter: ({ cellValue }) => RESET_CYCLE_OPTIONS[cellValue]?.label ?? '',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 80,
      formatter: ({ cellValue }) => STATUS_OPTIONS[cellValue]?.label ?? '',
    },
    { field: 'remark', title: '备注', minWidth: 150 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
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
