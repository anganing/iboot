import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWaveApi } from '#/api/wms/wave';

import { getDictOptions } from '@vben/hooks';

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
      fieldName: 'waveNo',
      label: '波次编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入波次编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'waveType',
      label: '波次类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择波次类型',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'planEndTime',
      label: '计划结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'WMS-出库波次数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'waveNo',
      label: '波次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入波次编号',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'waveType',
      label: '波次类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择波次类型',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'planEndTime',
      label: '计划结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<WmsWaveApi.Wave>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'waveNo',
      title: '波次编号',
      minWidth: 120,
    },
    {
      field: 'waveType',
      title: '波次类型',
      minWidth: 120,
    },
    {
      field: 'planStartTime',
      title: '计划开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'planEndTime',
      title: '计划结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：DRAFT、RELEASED、PICKING、DONE、CANCELLED',
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
      field: 'whCode',
      title: '仓库编码',
      minWidth: 120,
    },
    {
      field: 'whName',
      title: '仓库名称',
      minWidth: 120,
    },
    {
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（WMS-波次明细） ====================

 /** 新增/修改的表单 */
export function useWaveLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'lineNo',
                            label: '行号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入行号',
                                },
                        },
                        {
                            fieldName: 'outOrderId',
                            label: '出库单编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入出库单编号',
                                },
                        },
                        {
                            fieldName: 'outOrderLineId',
                            label: '出库单明细编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入出库单明细编号',
                                },
                        },
                        {
                            fieldName: 'priority',
                            label: '波次内优先级，数值越大越优先',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入波次内优先级，数值越大越优先',
                                },
                        },
                        {
                            fieldName: 'status',
                            label: '明细状态',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: [],
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
export function useWaveLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'waveId',
                    label: '波次编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入波次编号',
                        },
                },
                {
                    fieldName: 'lineNo',
                    label: '行号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入行号',
                        },
                },
                {
                    fieldName: 'outOrderId',
                    label: '出库单编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入出库单编号',
                        },
                },
                {
                    fieldName: 'outOrderLineId',
                    label: '出库单明细编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入出库单明细编号',
                        },
                },
                {
                    fieldName: 'priority',
                    label: '波次内优先级，数值越大越优先',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入波次内优先级，数值越大越优先',
                        },
                },
                {
                    fieldName: 'status',
                    label: '明细状态',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择明细状态',
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
export function useWaveLineGridColumns(): VxeTableGridOptions<WmsWaveApi.WaveLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'waveId',
                    title: '波次编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
                    minWidth: 120,
                },
                {
                    field: 'outOrderId',
                    title: '出库单编号',
                    minWidth: 120,
                },
                {
                    field: 'outOrderLineId',
                    title: '出库单明细编号',
                    minWidth: 120,
                },
                {
                    field: 'priority',
                    title: '波次内优先级，数值越大越优先',
                    minWidth: 120,
                },
                {
                    field: 'status',
                    title: '明细状态',
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