import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

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
      fieldName: 'reqNo',
      label: '领退补料申请单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入领退补料申请单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单编号',
      },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单号快照',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示工单级申请',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号，0表示工单级申请',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序级申请',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号，0表示非工序级申请',
      },
    },
    {
      fieldName: 'reqType',
      label: '申请类型：ISSUE、RETURN、SUPPLEMENT',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择申请类型：ISSUE、RETURN、SUPPLEMENT',
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
      label: '目标仓库或来源仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入目标仓库或来源仓库编号',
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
      fieldName: 'wmsBizNo',
      label: 'WMS生成的出入库单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入WMS生成的出入库单号',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED',
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
      label: 'MES-领退补料申请数据',
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
      fieldName: 'reqNo',
      label: '领退补料申请单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入领退补料申请单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单编号',
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
      fieldName: 'workOrderId',
      label: '作业工单编号，0表示工单级申请',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单编号，0表示工单级申请',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号，0表示非工序级申请',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务编号，0表示非工序级申请',
      },
    },
    {
      fieldName: 'reqType',
      label: '申请类型：ISSUE、RETURN、SUPPLEMENT',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择申请类型：ISSUE、RETURN、SUPPLEMENT',
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
      label: '目标仓库或来源仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入目标仓库或来源仓库编号',
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
      fieldName: 'wmsBizNo',
      label: 'WMS生成的出入库单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入WMS生成的出入库单号',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReq>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'reqNo',
      title: '领退补料申请单号',
      minWidth: 120,
    },
    {
      field: 'prodOrderNo',
      title: '生产工单号快照',
      minWidth: 120,
    },
    {
      field: 'reqType',
      title: '申请类型：ISSUE、RETURN、SUPPLEMENT',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'whId',
      title: '目标仓库或来源仓库编号',
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
      field: 'wmsBizNo',
      title: 'WMS生成的出入库单号',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED',
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
      field: 'prodOrderOrder_no',
      title: '生产工单order_no',
      minWidth: 120,
    },
    {
      field: 'workOrderWork_order_no',
      title: '作业工单编号，0表示工单级申请work_order_no',
      minWidth: 120,
    },
    {
      field: 'opTaskTask_no',
      title: '工序任务编号，0表示非工序级申请task_no',
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

// ==================== 子表（MES-领退补料申请明细） ====================

 /** 新增/修改的表单 */
export function useMaterialReqLineFormSchema(): VbenFormSchema[] {
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
                            fieldName: 'routeOpMatId',
                            label: '路线工序用料编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入路线工序用料编号',
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
                            fieldName: 'qty',
                            label: '申请数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入申请数量',
                                },
                        },
                        {
                            fieldName: 'processedQty',
                            label: 'WMS已处理数量',
                                rules: 'required',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入WMS已处理数量',
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
                            fieldName: 'batchId',
                            label: '指定退料批次编号',
                                component: 'Input',
                                componentProps: {
                                    placeholder: '请输入指定退料批次编号',
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
                            fieldName: 'mustIssueBeforeStart',
                            label: '是否必须在开工前发料',
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
export function useMaterialReqLineGridFormSchema(): VbenFormSchema[] {
    return [
                {
                    fieldName: 'reqId',
                    label: '领退补料申请编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入领退补料申请编号',
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
                    fieldName: 'routeOpMatId',
                    label: '路线工序用料编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入路线工序用料编号',
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
                    fieldName: 'qty',
                    label: '申请数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入申请数量',
                        },
                },
                {
                    fieldName: 'processedQty',
                    label: 'WMS已处理数量',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入WMS已处理数量',
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
                    fieldName: 'batchId',
                    label: '指定退料批次编号',
                        component: 'Input',
                        componentProps: {
                            clearable: true,
                            placeholder: '请输入指定退料批次编号',
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
                    fieldName: 'mustIssueBeforeStart',
                    label: '是否必须在开工前发料',
                        component: 'Select',
                        componentProps: {
                            clearable: true,
                                options: [],
                            placeholder: '请选择是否必须在开工前发料',
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
export function useMaterialReqLineGridColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReqLine>['columns'] {
    return [
            { type: 'checkbox', width: 40 },
                {
                    field: 'id',
                    title: '主键',
                    minWidth: 120,
                },
                {
                    field: 'reqId',
                    title: '领退补料申请编号',
                    minWidth: 120,
                },
                {
                    field: 'lineNo',
                    title: '行号',
                    minWidth: 120,
                },
                {
                    field: 'routeOpMatId',
                    title: '路线工序用料编号',
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
                    field: 'qty',
                    title: '申请数量',
                    minWidth: 120,
                },
                {
                    field: 'processedQty',
                    title: 'WMS已处理数量',
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
                    field: 'batchId',
                    title: '指定退料批次编号',
                    minWidth: 120,
                },
                {
                    field: 'lotNo',
                    title: '批次号快照',
                    minWidth: 120,
                },
                {
                    field: 'mustIssueBeforeStart',
                    title: '是否必须在开工前发料',
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