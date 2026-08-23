import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmRouteApi } from '#/api/mdm/route';

import { MDM_BOOLEAN_OPTIONS } from '../constants';

import { getRangePickerDefaultProps } from '#/utils';

const versionStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
];

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
      fieldName: 'version',
      label: '版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: versionStatusOptions,
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'effectiveDate',
      label: '生效日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
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
      fieldName: 'version',
      label: '版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: versionStatusOptions,
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'effectiveDate',
      label: '生效日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<MdmRouteApi.Route>['columns'] {
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
      field: 'version',
      title: '版本号',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
    },
    {
      field: 'effectiveDate',
      title: '生效日期',
      minWidth: 120,
    },
    {
      field: 'expiryDate',
      title: '失效日期',
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

// ==================== 子表（工艺路线工序） ====================

 /** 新增/修改的表单 */
export function useRouteOpFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'seq',
                            label: '工序顺序',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入工序顺序',
                                },
                        },
                        {
                            fieldName: 'opId',
                            label: '标准工序编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入标准工序编号',
                                },
                        },
                        {
                            fieldName: 'wcId',
                            label: '工作中心编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入工作中心编号',
                                },
                        },
                        {
                            fieldName: 'parallelGroup',
                            label: '并行组',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入并行组',
                                },
                        },
                        {
                            fieldName: 'keyFlag',
                            label: '是否关键工序',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: MDM_BOOLEAN_OPTIONS,
                                    buttonStyle: 'solid',
                                    optionType: 'button',
                                },
                        },
                        {
                            fieldName: 'faiFlag',
                            label: '是否首件检验',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: MDM_BOOLEAN_OPTIONS,
                                    buttonStyle: 'solid',
                                    optionType: 'button',
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
export function useRouteOpGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'routeId',
                    label: '工艺路线编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入工艺路线编号',
                        },
                },
                {
                    fieldName: 'seq',
                    label: '工序顺序',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入工序顺序',
                        },
                },
                {
                    fieldName: 'opId',
                    label: '标准工序编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入标准工序编号',
                        },
                },
                {
                    fieldName: 'wcId',
                    label: '工作中心编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入工作中心编号',
                        },
                },
                {
                    fieldName: 'parallelGroup',
                    label: '并行组',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入并行组',
                        },
                },
                {
                    fieldName: 'keyFlag',
                    label: '是否关键工序',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: MDM_BOOLEAN_OPTIONS,
                            placeholder: '请选择是否关键工序',
                        },
                },
                {
                    fieldName: 'faiFlag',
                    label: '是否首件检验',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: MDM_BOOLEAN_OPTIONS,
                            placeholder: '请选择是否首件检验',
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
export function useRouteOpGridColumns(): VxeTableGridOptions<MdmRouteApi.RouteOp>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'routeId',
                    title: '工艺路线编号',
                    minWidth: 120,
                },
                {
                    field: 'seq',
                    title: '工序顺序',
                    minWidth: 120,
                },
                {
                    field: 'opId',
                    title: '标准工序编号',
                    minWidth: 120,
                },
                {
                    field: 'wcId',
                    title: '工作中心编号',
                    minWidth: 120,
                },
                {
                    field: 'parallelGroup',
                    title: '并行组',
                    minWidth: 120,
                },
                {
                    field: 'keyFlag',
                    title: '是否关键工序',
                    minWidth: 120,
                },
                {
                    field: 'faiFlag',
                    title: '是否首件检验',
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


