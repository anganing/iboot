import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmUomConvApi } from '#/api/mdm/uomconv';


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
      fieldName: 'fromUomId',
      label: '来源计量单位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源计量单位编号',
      },
    },
    {
      fieldName: 'toUomId',
      label: '目标计量单位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标计量单位编号',
      },
    },
    {
      fieldName: 'ratio',
      label: '换算比例',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入换算比例',
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
      fieldName: 'fromUomId',
      label: '来源计量单位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源计量单位编号',
      },
    },
    {
      fieldName: 'toUomId',
      label: '目标计量单位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标计量单位编号',
      },
    },
    {
      fieldName: 'ratio',
      label: '换算比例',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入换算比例',
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
export function useGridColumns(): VxeTableGridOptions<MdmUomConvApi.UomConv>['columns'] {
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
      field: 'fromUomId',
      title: '来源计量单位编号',
      minWidth: 120,
    },
    {
      field: 'toUomId',
      title: '目标计量单位编号',
      minWidth: 120,
    },
    {
      field: 'ratio',
      title: '换算比例',
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

