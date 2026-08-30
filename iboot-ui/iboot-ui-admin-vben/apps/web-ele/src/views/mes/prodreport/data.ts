import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdReportApi } from '#/api/mes/prodreport';

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
      fieldName: 'reportNo',
      label: '报工单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入报工单号',
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
      fieldName: 'workOrderId',
      label: '作业工单编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工序任务编号',
      },
    },
    {
      fieldName: 'workerId',
      label: '报工作业人员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入报工作业人员编号',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产资源编号',
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入班次编号',
      },
    },
    {
      fieldName: 'qtyGood',
      label: '合格数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合格数量',
      },
    },
    {
      fieldName: 'qtyScrap',
      label: '报废数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入报废数量',
      },
    },
    {
      fieldName: 'qtyRework',
      label: '返工数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工数量',
      },
    },
    {
      fieldName: 'startTime',
      label: '本次作业开始时间',
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
      fieldName: 'endTime',
      label: '本次作业结束时间',
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
      fieldName: 'reportTime',
      label: '报工时间',
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
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED',
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
      label: '报工幂等键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入报工幂等键',
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
      label: 'MES-生产报工记录数据',
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
      fieldName: 'reportNo',
      label: '报工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报工单号',
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
      fieldName: 'workOrderId',
      label: '作业工单编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单编号',
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
      fieldName: 'workerId',
      label: '报工作业人员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报工作业人员编号',
      },
    },
    {
      fieldName: 'resId',
      label: '生产资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产资源编号',
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入班次编号',
      },
    },
    {
      fieldName: 'qtyGood',
      label: '合格数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入合格数量',
      },
    },
    {
      fieldName: 'qtyScrap',
      label: '报废数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报废数量',
      },
    },
    {
      fieldName: 'qtyRework',
      label: '返工数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工数量',
      },
    },
    {
      fieldName: 'startTime',
      label: '本次作业开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'endTime',
      label: '本次作业结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'reportTime',
      label: '报工时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '报工幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入报工幂等键',
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
export function useGridColumns(): VxeTableGridOptions<MesProdReportApi.ProdReport>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'reportNo',
      title: '报工单号',
      minWidth: 120,
    },
    {
      field: 'workerId',
      title: '报工作业人员编号',
      minWidth: 120,
    },
    {
      field: 'resId',
      title: '生产资源编号',
      minWidth: 120,
    },
    {
      field: 'shiftId',
      title: '班次编号',
      minWidth: 120,
    },
    {
      field: 'qtyGood',
      title: '合格数量',
      minWidth: 120,
    },
    {
      field: 'qtyScrap',
      title: '报废数量',
      minWidth: 120,
    },
    {
      field: 'qtyRework',
      title: '返工数量',
      minWidth: 120,
    },
    {
      field: 'startTime',
      title: '本次作业开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '本次作业结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'reportTime',
      title: '报工时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '报工幂等键',
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
      title: '作业工单work_order_no',
      minWidth: 120,
    },
    {
      field: 'opTaskTask_no',
      title: '工序任务task_no',
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