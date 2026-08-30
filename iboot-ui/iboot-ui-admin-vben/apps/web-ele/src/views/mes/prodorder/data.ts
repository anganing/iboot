import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesProdOrderApi } from '#/api/mes/prodorder';

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
      fieldName: 'orderNo',
      label: '生产工单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'demandId',
      label: '生产需求编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产需求编号',
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
      fieldName: 'workshopId',
      label: '车间编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入车间编号',
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
      label: '生产物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入生产物料编号',
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
      label: '计划生产数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入计划生产数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入已完成数量',
      },
    },
    {
      fieldName: 'scrapQty',
      label: '累计报废数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入累计报废数量',
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
      fieldName: 'bomId',
      label: '锁定的BOM编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的BOM编号',
      },
    },
    {
      fieldName: 'bomVersion',
      label: '锁定的BOM版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的BOM版本',
      },
    },
    {
      fieldName: 'routeId',
      label: '锁定的工艺路线编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的工艺路线编号',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '锁定的工艺路线版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的工艺路线版本',
      },
    },
    {
      fieldName: 'dueDate',
      label: '工单交期',
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
      label: '优先级',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入优先级',
      },
    },
    {
      fieldName: 'kitStatus',
      label: '齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE',
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
      label: '状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'releaseTime',
      label: '下达时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'actualStartTime',
      label: '实际开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'actualEndTime',
      label: '实际结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入乐观锁版本',
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
      label: 'MES-生产工单数据',
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
      fieldName: 'orderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产工单号',
      },
    },
    {
      fieldName: 'demandId',
      label: '生产需求编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产需求编号',
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
      fieldName: 'workshopId',
      label: '车间编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入车间编号',
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
      label: '生产物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入生产物料编号',
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
      label: '计划生产数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入计划生产数量',
      },
    },
    {
      fieldName: 'completedQty',
      label: '已完成数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入已完成数量',
      },
    },
    {
      fieldName: 'scrapQty',
      label: '累计报废数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入累计报废数量',
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
      fieldName: 'bomId',
      label: '锁定的BOM编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的BOM编号',
      },
    },
    {
      fieldName: 'bomVersion',
      label: '锁定的BOM版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的BOM版本',
      },
    },
    {
      fieldName: 'routeId',
      label: '锁定的工艺路线编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的工艺路线编号',
      },
    },
    {
      fieldName: 'routeVersion',
      label: '锁定的工艺路线版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的工艺路线版本',
      },
    },
    {
      fieldName: 'dueDate',
      label: '工单交期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'priority',
      label: '优先级',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入优先级',
      },
    },
    {
      fieldName: 'kitStatus',
      label: '齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE',
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED',
      },
    },
    {
      fieldName: 'releaseTime',
      label: '下达时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'actualStartTime',
      label: '实际开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'actualEndTime',
      label: '实际结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'version',
      label: '乐观锁版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入乐观锁版本',
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
export function useGridColumns(): VxeTableGridOptions<MesProdOrderApi.ProdOrder>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'orderNo',
      title: '生产工单号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'workshopId',
      title: '车间编号',
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
      title: '生产物料编号',
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
      title: '计划生产数量',
      minWidth: 120,
    },
    {
      field: 'completedQty',
      title: '已完成数量',
      minWidth: 120,
    },
    {
      field: 'scrapQty',
      title: '累计报废数量',
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
      field: 'bomId',
      title: '锁定的BOM编号',
      minWidth: 120,
    },
    {
      field: 'bomVersion',
      title: '锁定的BOM版本',
      minWidth: 120,
    },
    {
      field: 'routeId',
      title: '锁定的工艺路线编号',
      minWidth: 120,
    },
    {
      field: 'routeVersion',
      title: '锁定的工艺路线版本',
      minWidth: 120,
    },
    {
      field: 'dueDate',
      title: '工单交期',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'priority',
      title: '优先级',
      minWidth: 120,
    },
    {
      field: 'kitStatus',
      title: '齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'releaseTime',
      title: '下达时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'actualStartTime',
      title: '实际开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'actualEndTime',
      title: '实际结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'version',
      title: '乐观锁版本',
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
      field: 'demandDemand_no',
      title: '生产需求demand_no',
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