import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmMatAltApi } from '#/api/mdm/matalt';


import { getRangePickerDefaultProps } from '#/utils';

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
      fieldName: 'matId',
      label: '物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编号',
      },
    },
    {
      fieldName: 'altMatId',
      label: '替代物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入替代物料编号',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级',
      },
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
      fieldName: 'matId',
      label: '物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编号',
      },
    },
    {
      fieldName: 'altMatId',
      label: '替代物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入替代物料编号',
      },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注',
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MdmMatAltApi.MatAlt>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
    {
      field: 'id',
      title: '主键',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '物料编号',
      minWidth: 120,
    },
    {
      field: 'altMatId',
      title: '替代物料编号',
      minWidth: 120,
    },
    {
      field: 'priority',
      title: '优先级',
      minWidth: 120,
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

