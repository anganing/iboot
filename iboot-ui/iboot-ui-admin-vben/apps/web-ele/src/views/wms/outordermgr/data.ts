import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsOutOrderApi } from '#/api/wms/outordermgr';

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
      fieldName: 'outNo',
      label: '出库单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入出库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT',
      },
    },
    {
      fieldName: 'sourceType',
      label: '直接来源类型',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择直接来源类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '直接来源业务单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入直接来源业务单号',
      },
    },
    {
      fieldName: 'sourceLineId',
      label: '直接来源明细编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入直接来源明细编号',
      },
    },
    {
      fieldName: 'originType',
      label: '原始来源类型',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择原始来源类型',
      },
    },
    {
      fieldName: 'originNo',
      label: '原始来源单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入原始来源单号',
      },
    },
    {
      fieldName: 'customerId',
      label: '客户编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入客户编号',
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
      fieldName: 'prodOrderNo',
      label: '生产工单号快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单号快照',
      },
    },
    {
      fieldName: 'workOrderNo',
      label: '作业工单号快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单号快照',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'materialReqNo',
      label: '领退补料申请单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入领退补料申请单号',
      },
    },
    {
      fieldName: 'transferNo',
      label: '转移或调拨单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入转移或调拨单号',
      },
    },
    {
      fieldName: 'countOrderNo',
      label: '盘点单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '来源库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '来源工厂编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源工厂编号',
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
      fieldName: 'whId',
      label: '来源仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源仓库编号',
      },
    },
    {
      fieldName: 'whCode',
      label: '来源仓库编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源仓库编码快照',
      },
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织编号，0表示非调拨',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标库存组织编号，0表示非调拨',
      },
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库编号，0表示非调拨',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标仓库编号，0表示非调拨',
      },
    },
    {
      fieldName: 'pickStrategyId',
      label: '拣选策略编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入拣选策略编号',
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
      label: '状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
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
      fieldName: 'bizAttrsJson',
      label: '低频扩展属性',
      component: 'Input',
      componentProps: {
        placeholder: '请输入低频扩展属性',
      },
    },
    {
      fieldName: 'remark',
      label: '备注或人工原因',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注或人工原因',
      },
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'WMS-出库单数据',
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
      fieldName: 'outNo',
      label: '出库单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入出库单号',
      },
    },
    {
      fieldName: 'bizType',
      label: '出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT',
      },
    },
    {
      fieldName: 'sourceType',
      label: '直接来源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择直接来源类型',
      },
    },
    {
      fieldName: 'sourceBizNo',
      label: '直接来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入直接来源业务单号',
      },
    },
    {
      fieldName: 'sourceLineId',
      label: '直接来源明细编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入直接来源明细编号',
      },
    },
    {
      fieldName: 'originType',
      label: '原始来源类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择原始来源类型',
      },
    },
    {
      fieldName: 'originNo',
      label: '原始来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入原始来源单号',
      },
    },
    {
      fieldName: 'customerId',
      label: '客户编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入客户编号',
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
      fieldName: 'prodOrderNo',
      label: '生产工单号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号快照',
      },
    },
    {
      fieldName: 'workOrderNo',
      label: '作业工单号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单号快照',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'materialReqNo',
      label: '领退补料申请单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入领退补料申请单号',
      },
    },
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
      fieldName: 'countOrderNo',
      label: '盘点单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '来源库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '来源工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源工厂编号',
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
      fieldName: 'whId',
      label: '来源仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源仓库编号',
      },
    },
    {
      fieldName: 'whCode',
      label: '来源仓库编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源仓库编码快照',
      },
    },
    {
      fieldName: 'toInvOrgId',
      label: '目标库存组织编号，0表示非调拨',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标库存组织编号，0表示非调拨',
      },
    },
    {
      fieldName: 'toWhId',
      label: '目标仓库编号，0表示非调拨',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标仓库编号，0表示非调拨',
      },
    },
    {
      fieldName: 'pickStrategyId',
      label: '拣选策略编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入拣选策略编号',
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
      label: '状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
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
      fieldName: 'bizAttrsJson',
      label: '低频扩展属性',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入低频扩展属性',
      },
    },
    {
      fieldName: 'remark',
      label: '备注或人工原因',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注或人工原因',
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
export function useGridColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'outNo',
      title: '出库单号',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT',
      minWidth: 120,
    },
    {
      field: 'sourceType',
      title: '直接来源类型',
      minWidth: 120,
    },
    {
      field: 'sourceBizNo',
      title: '直接来源业务单号',
      minWidth: 120,
    },
    {
      field: 'sourceLineId',
      title: '直接来源明细编号',
      minWidth: 120,
    },
    {
      field: 'originType',
      title: '原始来源类型',
      minWidth: 120,
    },
    {
      field: 'originNo',
      title: '原始来源单号',
      minWidth: 120,
    },
    {
      field: 'customerId',
      title: '客户编号',
      minWidth: 120,
    },
    {
      field: 'supplierId',
      title: '供应商编号',
      minWidth: 120,
    },
    {
      field: 'prodOrderNo',
      title: '生产工单号快照',
      minWidth: 120,
    },
    {
      field: 'workOrderNo',
      title: '作业工单号快照',
      minWidth: 120,
    },
    {
      field: 'opTaskId',
      title: '工序任务编号',
      minWidth: 120,
    },
    {
      field: 'materialReqNo',
      title: '领退补料申请单号',
      minWidth: 120,
    },
    {
      field: 'transferNo',
      title: '转移或调拨单号',
      minWidth: 120,
    },
    {
      field: 'countOrderNo',
      title: '盘点单号',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '来源库存组织编号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '来源工厂编号',
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
      field: 'whCode',
      title: '来源仓库编码快照',
      minWidth: 120,
    },
    {
      field: 'toInvOrgId',
      title: '目标库存组织编号，0表示非调拨',
      minWidth: 120,
    },
    {
      field: 'toWhId',
      title: '目标仓库编号，0表示非调拨',
      minWidth: 120,
    },
    {
      field: 'pickStrategyId',
      title: '拣选策略编号',
      minWidth: 120,
    },
    {
      field: 'hookMode',
      title: '外部设备触发方式：NONE、WCS、AGV',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '创建幂等键',
      minWidth: 120,
    },
    {
      field: 'bizAttrsJson',
      title: '低频扩展属性',
      minWidth: 120,
    },
    {
      field: 'remark',
      title: '备注或人工原因',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'whName',
      title: '来源仓库名称',
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

// ==================== 子表（WMS-出库单明细） ====================

 /** 新增/修改的表单 */
export function useOutOrderLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'sourceLineId',
                            label: '来源明细编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源明细编号',
                                },
                        },
                        {
                            fieldName: 'sourceLineNo',
                            label: '来源明细行号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源明细行号',
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
                            fieldName: 'batchId',
                            label: '指定或分配的批次编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入指定或分配的批次编号',
                                },
                        },
                        {
                            fieldName: 'lotNo',
                            label: '批次号快照',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入批次号快照',
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
                            fieldName: 'fromLocId',
                            label: '来源库位编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源库位编号',
                                },
                        },
                        {
                            fieldName: 'fromLocCode',
                            label: '来源库位编码快照',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入来源库位编码快照',
                                },
                        },
                        {
                            fieldName: 'toLocId',
                            label: '目标库位编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入目标库位编号',
                                },
                        },
                        {
                            fieldName: 'qualityStatus',
                            label: '要求的库存质量状态',
                                rules: 'required',
                                component: 'RadioGroup',
                                componentProps: {
                                        options: [],
                                    buttonStyle: 'solid',
                                    optionType: 'button',
                                },
                        },
                        {
                            fieldName: 'planQty',
                            label: '计划出库数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入计划出库数量',
                                },
                        },
                        {
                            fieldName: 'allocatedQty',
                            label: '已分配数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入已分配数量',
                                },
                        },
                        {
                            fieldName: 'pickedQty',
                            label: '已拣选数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入已拣选数量',
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
                            fieldName: 'containerId',
                            label: '容器编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入容器编号',
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
export function useOutOrderLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'orderId',
                    label: '出库单编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入出库单编号',
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
                    fieldName: 'sourceLineId',
                    label: '来源明细编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源明细编号',
                        },
                },
                {
                    fieldName: 'sourceLineNo',
                    label: '来源明细行号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源明细行号',
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
                    fieldName: 'batchId',
                    label: '指定或分配的批次编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入指定或分配的批次编号',
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
                    fieldName: 'fromLocId',
                    label: '来源库位编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源库位编号',
                        },
                },
                {
                    fieldName: 'fromLocCode',
                    label: '来源库位编码快照',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入来源库位编码快照',
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
                    fieldName: 'qualityStatus',
                    label: '要求的库存质量状态',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择要求的库存质量状态',
                        },
                },
                {
                    fieldName: 'planQty',
                    label: '计划出库数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入计划出库数量',
                        },
                },
                {
                    fieldName: 'allocatedQty',
                    label: '已分配数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入已分配数量',
                        },
                },
                {
                    fieldName: 'pickedQty',
                    label: '已拣选数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入已拣选数量',
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
                    fieldName: 'containerId',
                    label: '容器编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入容器编号',
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
export function useOutOrderLineGridColumns(): VxeTableGridOptions<WmsOutOrderApi.OutOrderLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'orderId',
                    title: '出库单编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
                    minWidth: 120,
                },
                {
                    field: 'sourceLineId',
                    title: '来源明细编号',
                    minWidth: 120,
                },
                {
                    field: 'sourceLineNo',
                    title: '来源明细行号',
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
                    field: 'batchId',
                    title: '指定或分配的批次编号',
                    minWidth: 120,
                },
                {
                    field: 'lotNo',
                    title: '批次号快照',
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
                    field: 'fromLocId',
                    title: '来源库位编号',
                    minWidth: 120,
                },
                {
                    field: 'fromLocCode',
                    title: '来源库位编码快照',
                    minWidth: 120,
                },
                {
                    field: 'toLocId',
                    title: '目标库位编号',
                    minWidth: 120,
                },
                {
                    field: 'qualityStatus',
                    title: '要求的库存质量状态',
                    minWidth: 120,
                },
                {
                    field: 'planQty',
                    title: '计划出库数量',
                    minWidth: 120,
                },
                {
                    field: 'allocatedQty',
                    title: '已分配数量',
                    minWidth: 120,
                },
                {
                    field: 'pickedQty',
                    title: '已拣选数量',
                    minWidth: 120,
                },
                {
                    field: 'postedQty',
                    title: '已过账数量',
                    minWidth: 120,
                },
                {
                    field: 'containerId',
                    title: '容器编号',
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