import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

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
      fieldName: 'transferNo',
      label: '转移或调拨单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入转移或调拨单号',
      },
    },
    {
      fieldName: 'transferType',
      label: '转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromInvOrgId',
      label: '来源库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源库存组织编号',
      },
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标库存组织编号',
      },
    },
    {
      fieldName: 'fromWhId',
      label: '来源仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源仓库编号',
      },
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标仓库编号',
      },
    },
    {
      fieldName: 'fromProjectId',
      label: '来源项目编号，0表示公共库存',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源项目编号，0表示公共库存',
      },
    },
    {
      fieldName: 'toProjectId',
      label: '目标项目编号，0表示公共库存',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标项目编号，0表示公共库存',
      },
    },
    {
      fieldName: 'hookMode',
      label: '外部设备触发方式：NONE、WCS、AGV',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部设备触发方式：NONE、WCS、AGV',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
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
      label: 'WMS-库存转移调拨单数据',
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
      fieldName: 'transferNo',
      label: '转移或调拨单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入转移或调拨单号',
      },
    },
    {
      fieldName: 'transferType',
      label: '转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'fromInvOrgId',
      label: '来源库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源库存组织编号',
      },
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标库存组织编号',
      },
    },
    {
      fieldName: 'fromWhId',
      label: '来源仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源仓库编号',
      },
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标仓库编号',
      },
    },
    {
      fieldName: 'fromProjectId',
      label: '来源项目编号，0表示公共库存',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源项目编号，0表示公共库存',
      },
    },
    {
      fieldName: 'toProjectId',
      label: '目标项目编号，0表示公共库存',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标项目编号，0表示公共库存',
      },
    },
    {
      fieldName: 'hookMode',
      label: '外部设备触发方式：NONE、WCS、AGV',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部设备触发方式：NONE、WCS、AGV',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
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
export function useGridColumns(): VxeTableGridOptions<WmsTransferOrderApi.TransferOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'transferNo',
      title: '转移或调拨单号',
      minWidth: 120,
    },
    {
      field: 'transferType',
      title: '转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER',
      minWidth: 120,
    },
    {
      field: 'sourceType',
      title: '来源业务类型',
      minWidth: 120,
    },
    {
      field: 'sourceBizNo',
      title: '来源业务单号',
      minWidth: 120,
    },
    {
      field: 'fromInvOrgId',
      title: '来源库存组织编号',
      minWidth: 120,
    },
    {
      field: 'toInvOrgId',
      title: '目标库存组织编号',
      minWidth: 120,
    },
    {
      field: 'fromWhId',
      title: '来源仓库编号',
      minWidth: 120,
    },
    {
      field: 'toWhId',
      title: '目标仓库编号',
      minWidth: 120,
    },
    {
      field: 'fromProjectId',
      title: '来源项目编号，0表示公共库存',
      minWidth: 120,
    },
    {
      field: 'toProjectId',
      title: '目标项目编号，0表示公共库存',
      minWidth: 120,
    },
    {
      field: 'hookMode',
      title: '外部设备触发方式：NONE、WCS、AGV',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
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
      title: '操作',
      width: 200,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（WMS-库存转移调拨明细） ====================

 /** 新增/修改的表单 */
export function useTransferOrderLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'containerId',
                            label: '容器编号，0表示散件',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入容器编号，0表示散件',
                                },
                        },
                        {
                            fieldName: 'fromLocId',
                            label: '来源库位编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源库位编号',
                                },
                        },
                        {
                            fieldName: 'toLocId',
                            label: '目标库位编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标库位编号',
                                },
                        },
                        {
                            fieldName: 'fromStockScope',
                            label: '来源库存范围',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源库存范围',
                                },
                        },
                        {
                            fieldName: 'toStockScope',
                            label: '目标库存范围',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标库存范围',
                                },
                        },
                        {
                            fieldName: 'fromProjectId',
                            label: '来源项目编号，0表示公共库存',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源项目编号，0表示公共库存',
                                },
                        },
                        {
                            fieldName: 'toProjectId',
                            label: '目标项目编号，0表示公共库存',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标项目编号，0表示公共库存',
                                },
                        },
                        {
                            fieldName: 'fromProjectTaskId',
                            label: '来源项目任务编号，0表示无项目任务',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源项目任务编号，0表示无项目任务',
                                },
                        },
                        {
                            fieldName: 'toProjectTaskId',
                            label: '目标项目任务编号，0表示无项目任务',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标项目任务编号，0表示无项目任务',
                                },
                        },
                        {
                            fieldName: 'qualityStatusFrom',
                            label: '来源质量状态',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源质量状态',
                                },
                        },
                        {
                            fieldName: 'qualityStatusTo',
                            label: '目标质量状态',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标质量状态',
                                },
                        },
                        {
                            fieldName: 'uomId',
                            label: '基准计量单位编号',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入基准计量单位编号',
                                },
                        },
                        {
                            fieldName: 'uomCode',
                            label: '基准单位编码快照',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入基准单位编码快照',
                                },
                        },
                        {
                            fieldName: 'planQty',
                            label: '计划转移数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入计划转移数量',
                                },
                        },
                        {
                            fieldName: 'movedQty',
                            label: '已搬运数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入已搬运数量',
                                },
                        },
                        {
                            fieldName: 'postedQty',
                            label: '已过账数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入已过账数量',
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
export function useTransferOrderLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'orderId',
                    label: '转移调拨单编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入转移调拨单编号',
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
                    fieldName: 'containerId',
                    label: '容器编号，0表示散件',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入容器编号，0表示散件',
                        },
                },
                {
                    fieldName: 'fromLocId',
                    label: '来源库位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源库位编号',
                        },
                },
                {
                    fieldName: 'toLocId',
                    label: '目标库位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入目标库位编号',
                        },
                },
                {
                    fieldName: 'fromStockScope',
                    label: '来源库存范围',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源库存范围',
                        },
                },
                {
                    fieldName: 'toStockScope',
                    label: '目标库存范围',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入目标库存范围',
                        },
                },
                {
                    fieldName: 'fromProjectId',
                    label: '来源项目编号，0表示公共库存',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源项目编号，0表示公共库存',
                        },
                },
                {
                    fieldName: 'toProjectId',
                    label: '目标项目编号，0表示公共库存',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入目标项目编号，0表示公共库存',
                        },
                },
                {
                    fieldName: 'fromProjectTaskId',
                    label: '来源项目任务编号，0表示无项目任务',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源项目任务编号，0表示无项目任务',
                        },
                },
                {
                    fieldName: 'toProjectTaskId',
                    label: '目标项目任务编号，0表示无项目任务',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入目标项目任务编号，0表示无项目任务',
                        },
                },
                {
                    fieldName: 'qualityStatusFrom',
                    label: '来源质量状态',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源质量状态',
                        },
                },
                {
                    fieldName: 'qualityStatusTo',
                    label: '目标质量状态',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入目标质量状态',
                        },
                },
                {
                    fieldName: 'uomId',
                    label: '基准计量单位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入基准计量单位编号',
                        },
                },
                {
                    fieldName: 'uomCode',
                    label: '基准单位编码快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入基准单位编码快照',
                        },
                },
                {
                    fieldName: 'planQty',
                    label: '计划转移数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入计划转移数量',
                        },
                },
                {
                    fieldName: 'movedQty',
                    label: '已搬运数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入已搬运数量',
                        },
                },
                {
                    fieldName: 'postedQty',
                    label: '已过账数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入已过账数量',
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
export function useTransferOrderLineGridColumns(): VxeTableGridOptions<WmsTransferOrderApi.TransferOrderLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'orderId',
                    title: '转移调拨单编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
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
                    field: 'containerId',
                    title: '容器编号，0表示散件',
                    minWidth: 120,
                },
                {
                    field: 'fromLocId',
                    title: '来源库位编号',
                    minWidth: 120,
                },
                {
                    field: 'toLocId',
                    title: '目标库位编号',
                    minWidth: 120,
                },
                {
                    field: 'fromStockScope',
                    title: '来源库存范围',
                    minWidth: 120,
                },
                {
                    field: 'toStockScope',
                    title: '目标库存范围',
                    minWidth: 120,
                },
                {
                    field: 'fromProjectId',
                    title: '来源项目编号，0表示公共库存',
                    minWidth: 120,
                },
                {
                    field: 'toProjectId',
                    title: '目标项目编号，0表示公共库存',
                    minWidth: 120,
                },
                {
                    field: 'fromProjectTaskId',
                    title: '来源项目任务编号，0表示无项目任务',
                    minWidth: 120,
                },
                {
                    field: 'toProjectTaskId',
                    title: '目标项目任务编号，0表示无项目任务',
                    minWidth: 120,
                },
                {
                    field: 'qualityStatusFrom',
                    title: '来源质量状态',
                    minWidth: 120,
                },
                {
                    field: 'qualityStatusTo',
                    title: '目标质量状态',
                    minWidth: 120,
                },
                {
                    field: 'uomId',
                    title: '基准计量单位编号',
                    minWidth: 120,
                },
                {
                    field: 'uomCode',
                    title: '基准单位编码快照',
                    minWidth: 120,
                },
                {
                    field: 'planQty',
                    title: '计划转移数量',
                    minWidth: 120,
                },
                {
                    field: 'movedQty',
                    title: '已搬运数量',
                    minWidth: 120,
                },
                {
                    field: 'postedQty',
                    title: '已过账数量',
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