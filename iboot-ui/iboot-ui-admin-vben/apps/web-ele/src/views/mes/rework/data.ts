import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesReworkApi } from '#/api/mes/rework';

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
      fieldName: 'reworkNo',
      label: '返工任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工任务编号',
      },
    },
    {
      fieldName: 'sourceQcTaskId',
      label: '来源质检任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源质检任务编号',
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
      component: 'Input',
      componentProps: {
        placeholder: '请输入作业工单编号',
      },
    },
    {
      fieldName: 'opTaskId',
      label: '返工起始工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工起始工序任务编号',
      },
    },
    {
      fieldName: 'qty',
      label: '返工数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工数量',
      },
    },
    {
      fieldName: 'routeId',
      label: '返工工艺路线编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工工艺路线编号',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '返工工艺路线版本',
      component: 'Input',
      componentProps: {
        placeholder: '请输入返工工艺路线版本',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED',
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
      label: 'MES-返工任务数据',
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
      fieldName: 'reworkNo',
      label: '返工任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工任务编号',
      },
    },
    {
      fieldName: 'sourceQcTaskId',
      label: '来源质检任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源质检任务编号',
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
      label: '返工起始工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工起始工序任务编号',
      },
    },
    {
      fieldName: 'qty',
      label: '返工数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工数量',
      },
    },
    {
      fieldName: 'routeId',
      label: '返工工艺路线编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工工艺路线编号',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '返工工艺路线版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入返工工艺路线版本',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<MesReworkApi.Rework>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'reworkNo',
      title: '返工任务编号',
      minWidth: 120,
    },
    {
      field: 'sourceQcTaskId',
      title: '来源质检任务编号',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '返工数量',
      minWidth: 120,
    },
    {
      field: 'routeId',
      title: '返工工艺路线编号',
      minWidth: 120,
    },
    {
      field: 'routeVersion',
      title: '返工工艺路线版本',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED',
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
      title: '返工起始工序任务task_no',
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