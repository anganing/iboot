import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

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
      fieldName: 'countNo',
      label: '盘点单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'countType',
      label: '盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存组织编号',
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
      fieldName: 'zoneId',
      label: '库区编号，0表示全部库区',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库区编号，0表示全部库区',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示不限项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目编号，0表示不限项目',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '账面快照时间',
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
      fieldName: 'blindCountFlag',
      label: '是否盲盘',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '创建幂等键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入创建幂等键',
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
      label: 'WMS-盘点单数据',
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
      fieldName: 'countNo',
      label: '盘点单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'countType',
      label: '盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存组织编号',
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
      fieldName: 'zoneId',
      label: '库区编号，0表示全部库区',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区编号，0表示全部库区',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示不限项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目编号，0表示不限项目',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '账面快照时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'blindCountFlag',
      label: '是否盲盘',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否盲盘',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '创建幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入创建幂等键',
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
export function useGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'countNo',
      title: '盘点单号',
      minWidth: 120,
    },
    {
      field: 'countType',
      title: '盘点类型：FULL、SAMPLE、ABC_CYCLE、DYNAMIC',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目编号，0表示不限项目',
      minWidth: 120,
    },
    {
      field: 'snapshotTime',
      title: '账面快照时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'blindCountFlag',
      title: '是否盲盘',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、FROZEN、COUNTING、DIFF_CONFIRMED、DONE、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '创建幂等键',
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
      field: 'zoneCode',
      title: '库区编号，0表示全部库区编码',
      minWidth: 120,
    },
    {
      field: 'zoneName',
      title: '库区编号，0表示全部库区名称',
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

// ==================== 子表（WMS-盘点明细） ====================

 /** 新增/修改的表单 */
export function useCountOrderLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'balanceId',
                            label: '库存余额编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入库存余额编号',
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
                            fieldName: 'matCode',
                            label: '物料编码快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入物料编码快照',
                                },
                        },
                        {
                            fieldName: 'matName',
                            label: '物料名称快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入物料名称快照',
                                },
                        },
                        {
                            fieldName: 'batchId',
                            label: '批次编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入批次编号',
                                },
                        },
                        {
                            fieldName: 'lotNo',
                            label: '批次号快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入批次号快照',
                                },
                        },
                        {
                            fieldName: 'locId',
                            label: '库位编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入库位编号',
                                },
                        },
                        {
                            fieldName: 'locCode',
                            label: '库位编码快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入库位编码快照',
                                },
                        },
                        {
                            fieldName: 'containerId',
                            label: '容器编号，0表示散件',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入容器编号，0表示散件',
                                },
                        },
                        {
                            fieldName: 'qualityStatus',
                            label: '质量状态',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: [],
                                    buttonStyle: 'solid',
                                    optionType: 'button',
                                },
                        },
                        {
                            fieldName: 'projectId',
                            label: '项目编号，0表示无项目',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入项目编号，0表示无项目',
                                },
                        },
                        {
                            fieldName: 'projectTaskId',
                            label: '项目任务编号，0表示无项目任务',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入项目任务编号，0表示无项目任务',
                                },
                        },
                        {
                            fieldName: 'bookQty',
                            label: '账面数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入账面数量',
                                },
                        },
                        {
                            fieldName: 'actualQty',
                            label: '实盘数量',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入实盘数量',
                                },
                        },
                        {
                            fieldName: 'diffQty',
                            label: '差异数量',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入差异数量',
                                },
                        },
                        {
                            fieldName: 'adjustBizNo',
                            label: '差异调整入出库单号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入差异调整入出库单号',
                                },
                        },
                        {
                            fieldName: 'status',
                            label: '明细状态：PENDING、COUNTED、DIFF、ADJUSTED',
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
export function useCountOrderLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'orderId',
                    label: '盘点单编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入盘点单编号',
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
                    fieldName: 'balanceId',
                    label: '库存余额编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入库存余额编号',
                        },
                },
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
                    fieldName: 'matCode',
                    label: '物料编码快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入物料编码快照',
                        },
                },
                {
                    fieldName: 'matName',
                    label: '物料名称快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入物料名称快照',
                        },
                },
                {
                    fieldName: 'batchId',
                    label: '批次编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入批次编号',
                        },
                },
                {
                    fieldName: 'lotNo',
                    label: '批次号快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入批次号快照',
                        },
                },
                {
                    fieldName: 'locId',
                    label: '库位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入库位编号',
                        },
                },
                {
                    fieldName: 'locCode',
                    label: '库位编码快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入库位编码快照',
                        },
                },
                {
                    fieldName: 'containerId',
                    label: '容器编号，0表示散件',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入容器编号，0表示散件',
                        },
                },
                {
                    fieldName: 'qualityStatus',
                    label: '质量状态',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择质量状态',
                        },
                },
                {
                    fieldName: 'projectId',
                    label: '项目编号，0表示无项目',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入项目编号，0表示无项目',
                        },
                },
                {
                    fieldName: 'projectTaskId',
                    label: '项目任务编号，0表示无项目任务',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入项目任务编号，0表示无项目任务',
                        },
                },
                {
                    fieldName: 'bookQty',
                    label: '账面数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入账面数量',
                        },
                },
                {
                    fieldName: 'actualQty',
                    label: '实盘数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入实盘数量',
                        },
                },
                {
                    fieldName: 'diffQty',
                    label: '差异数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入差异数量',
                        },
                },
                {
                    fieldName: 'adjustBizNo',
                    label: '差异调整入出库单号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入差异调整入出库单号',
                        },
                },
                {
                    fieldName: 'status',
                    label: '明细状态：PENDING、COUNTED、DIFF、ADJUSTED',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择明细状态：PENDING、COUNTED、DIFF、ADJUSTED',
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
export function useCountOrderLineGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'orderId',
                    title: '盘点单编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
                    minWidth: 120,
                },
                {
                    field: 'balanceId',
                    title: '库存余额编号',
                    minWidth: 120,
                },
                {
                    field: 'matId',
                    title: '物料编号',
                    minWidth: 120,
                },
                {
                    field: 'matCode',
                    title: '物料编码快照',
                    minWidth: 120,
                },
                {
                    field: 'matName',
                    title: '物料名称快照',
                    minWidth: 120,
                },
                {
                    field: 'batchId',
                    title: '批次编号',
                    minWidth: 120,
                },
                {
                    field: 'lotNo',
                    title: '批次号快照',
                    minWidth: 120,
                },
                {
                    field: 'locId',
                    title: '库位编号',
                    minWidth: 120,
                },
                {
                    field: 'locCode',
                    title: '库位编码快照',
                    minWidth: 120,
                },
                {
                    field: 'containerId',
                    title: '容器编号，0表示散件',
                    minWidth: 120,
                },
                {
                    field: 'qualityStatus',
                    title: '质量状态',
                    minWidth: 120,
                },
                {
                    field: 'projectId',
                    title: '项目编号，0表示无项目',
                    minWidth: 120,
                },
                {
                    field: 'projectTaskId',
                    title: '项目任务编号，0表示无项目任务',
                    minWidth: 120,
                },
                {
                    field: 'bookQty',
                    title: '账面数量',
                    minWidth: 120,
                },
                {
                    field: 'actualQty',
                    title: '实盘数量',
                    minWidth: 120,
                },
                {
                    field: 'diffQty',
                    title: '差异数量',
                    minWidth: 120,
                },
                {
                    field: 'adjustBizNo',
                    title: '差异调整入出库单号',
                    minWidth: 120,
                },
                {
                    field: 'status',
                    title: '明细状态：PENDING、COUNTED、DIFF、ADJUSTED',
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