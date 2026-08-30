import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsArrivalApi } from '#/api/wms/arrival';

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
      fieldName: 'arrivalNo',
      label: '到货登记单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入到货登记单号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源单号',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商编号',
      },
    },
    {
      fieldName: 'supplierCode',
      label: '供应商编码快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商编码快照',
      },
    },
    {
      fieldName: 'supplierName',
      label: '供应商名称快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商名称快照',
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
      fieldName: 'plantId',
      label: '工厂编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'whId',
      label: '目标仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标仓库编号',
      },
    },
    {
      fieldName: 'expectDate',
      label: '预计到货日期',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'arrivalTime',
      label: '实际到货时间',
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
      label: '状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED',
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
      label: 'WMS-到货登记数据',
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
      fieldName: 'arrivalNo',
      label: '到货登记单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入到货登记单号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源单号',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商编号',
      },
    },
    {
      fieldName: 'supplierCode',
      label: '供应商编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商编码快照',
      },
    },
    {
      fieldName: 'supplierName',
      label: '供应商名称快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商名称快照',
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
      fieldName: 'plantId',
      label: '工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'whId',
      label: '目标仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标仓库编号',
      },
    },
    {
      fieldName: 'expectDate',
      label: '预计到货日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'arrivalTime',
      label: '实际到货时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<WmsArrivalApi.Arrival>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'arrivalNo',
      title: '到货登记单号',
      minWidth: 120,
    },
    {
      field: 'sourceType',
      title: '来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER',
      minWidth: 120,
    },
    {
      field: 'sourceNo',
      title: '来源单号',
      minWidth: 120,
    },
    {
      field: 'supplierId',
      title: '供应商编号',
      minWidth: 120,
    },
    {
      field: 'supplierCode',
      title: '供应商编码快照',
      minWidth: 120,
    },
    {
      field: 'supplierName',
      title: '供应商名称快照',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'expectDate',
      title: '预计到货日期',
      minWidth: 120,
    },
    {
      field: 'arrivalTime',
      title: '实际到货时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED',
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
      title: '目标仓库编码',
      minWidth: 120,
    },
    {
      field: 'whName',
      title: '目标仓库名称',
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

// ==================== 子表（WMS-到货登记明细） ====================

 /** 新增/修改的表单 */
export function useArrivalLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'sourceLineNo',
                            label: '来源单据行号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源单据行号',
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
                            fieldName: 'matSpec',
                            label: '物料规格快照',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入物料规格快照',
                                },
                        },
                        {
                            fieldName: 'lotNo',
                            label: '到货批次号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入到货批次号',
                                },
                        },
                        {
                            fieldName: 'supplierLotNo',
                            label: '供应商批次号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入供应商批次号',
                                },
                        },
                        {
                            fieldName: 'arrivalQty',
                            label: '到货数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入到货数量',
                                },
                        },
                        {
                            fieldName: 'uomId',
                            label: '计量单位编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入计量单位编号',
                                },
                        },
                        {
                            fieldName: 'uomCode',
                            label: '计量单位编码快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入计量单位编码快照',
                                },
                        },
                        {
                            fieldName: 'needQc',
                            label: '是否需要质检',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: [],
                                    buttonStyle: 'solid',
                                    optionType: 'button',
                                },
                        },
                        {
                            fieldName: 'iqcMode',
                            label: '来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING',
                                },
                        },
                        {
                            fieldName: 'qcTaskId',
                            label: '质检任务编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入质检任务编号',
                                },
                        },
                        {
                            fieldName: 'qualifiedQty',
                            label: '合格数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入合格数量',
                                },
                        },
                        {
                            fieldName: 'rejectedQty',
                            label: '不合格数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入不合格数量',
                                },
                        },
                        {
                            fieldName: 'status',
                            label: '状态：PENDING、QC_PENDING、READY、DONE、CANCELLED',
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
export function useArrivalLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'arrivalId',
                    label: '到货登记编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入到货登记编号',
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
                    fieldName: 'sourceLineNo',
                    label: '来源单据行号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源单据行号',
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
                    fieldName: 'matSpec',
                    label: '物料规格快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入物料规格快照',
                        },
                },
                {
                    fieldName: 'lotNo',
                    label: '到货批次号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入到货批次号',
                        },
                },
                {
                    fieldName: 'supplierLotNo',
                    label: '供应商批次号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入供应商批次号',
                        },
                },
                {
                    fieldName: 'arrivalQty',
                    label: '到货数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入到货数量',
                        },
                },
                {
                    fieldName: 'uomId',
                    label: '计量单位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入计量单位编号',
                        },
                },
                {
                    fieldName: 'uomCode',
                    label: '计量单位编码快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入计量单位编码快照',
                        },
                },
                {
                    fieldName: 'needQc',
                    label: '是否需要质检',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择是否需要质检',
                        },
                },
                {
                    fieldName: 'iqcMode',
                    label: '来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING',
                        },
                },
                {
                    fieldName: 'qcTaskId',
                    label: '质检任务编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入质检任务编号',
                        },
                },
                {
                    fieldName: 'qualifiedQty',
                    label: '合格数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入合格数量',
                        },
                },
                {
                    fieldName: 'rejectedQty',
                    label: '不合格数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入不合格数量',
                        },
                },
                {
                    fieldName: 'status',
                    label: '状态：PENDING、QC_PENDING、READY、DONE、CANCELLED',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择状态：PENDING、QC_PENDING、READY、DONE、CANCELLED',
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
export function useArrivalLineGridColumns(): VxeTableGridOptions<WmsArrivalApi.ArrivalLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'arrivalId',
                    title: '到货登记编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
                    minWidth: 120,
                },
                {
                    field: 'sourceLineNo',
                    title: '来源单据行号',
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
                    field: 'matSpec',
                    title: '物料规格快照',
                    minWidth: 120,
                },
                {
                    field: 'lotNo',
                    title: '到货批次号',
                    minWidth: 120,
                },
                {
                    field: 'supplierLotNo',
                    title: '供应商批次号',
                    minWidth: 120,
                },
                {
                    field: 'arrivalQty',
                    title: '到货数量',
                    minWidth: 120,
                },
                {
                    field: 'uomId',
                    title: '计量单位编号',
                    minWidth: 120,
                },
                {
                    field: 'uomCode',
                    title: '计量单位编码快照',
                    minWidth: 120,
                },
                {
                    field: 'needQc',
                    title: '是否需要质检',
                    minWidth: 120,
                },
                {
                    field: 'iqcMode',
                    title: '来料检验模式：PRE_INBOUND、POST_INBOUND_PENDING',
                    minWidth: 120,
                },
                {
                    field: 'qcTaskId',
                    title: '质检任务编号',
                    minWidth: 120,
                },
                {
                    field: 'qualifiedQty',
                    title: '合格数量',
                    minWidth: 120,
                },
                {
                    field: 'rejectedQty',
                    title: '不合格数量',
                    minWidth: 120,
                },
                {
                    field: 'status',
                    title: '状态：PENDING、QC_PENDING、READY、DONE、CANCELLED',
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