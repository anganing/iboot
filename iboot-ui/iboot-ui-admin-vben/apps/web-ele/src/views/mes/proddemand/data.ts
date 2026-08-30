import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdDemandApi } from '#/api/mes/proddemand';

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
      fieldName: 'demandNo',
      label: '生产需求编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产需求编号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：MANUAL、EXCEL、ERP、APS',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源类型：MANUAL、EXCEL、ERP、APS',
      },
    },
    {
      fieldName: 'sourceNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源单号',
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
      fieldName: 'matId',
      label: '需求物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入需求物料编号',
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
      label: '需求数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入需求数量',
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
      fieldName: 'dueDate',
      label: '需求交期',
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
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级，数值越大越优先',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED',
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
      label: 'MES-生产需求数据',
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
      fieldName: 'demandNo',
      label: '生产需求编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产需求编号',
      },
    },
    {
      fieldName: 'sourceType',
      label: '来源类型：MANUAL、EXCEL、ERP、APS',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源类型：MANUAL、EXCEL、ERP、APS',
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
      fieldName: 'plantId',
      label: '工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂编号',
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
      fieldName: 'matId',
      label: '需求物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入需求物料编号',
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
      label: '需求数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入需求数量',
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
      fieldName: 'dueDate',
      label: '需求交期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'priority',
      label: '优先级，数值越大越优先',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级，数值越大越优先',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED',
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
export function useGridColumns(): VxeTableGridOptions<MesProdDemandApi.ProdDemand>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'demandNo',
      title: '生产需求编号',
      minWidth: 120,
    },
    {
      field: 'sourceType',
      title: '来源类型：MANUAL、EXCEL、ERP、APS',
      minWidth: 120,
    },
    {
      field: 'sourceNo',
      title: '来源单号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
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
      field: 'matId',
      title: '需求物料编号',
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
      title: '需求数量',
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
      field: 'dueDate',
      title: '需求交期',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'priority',
      title: '优先级，数值越大越优先',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED',
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