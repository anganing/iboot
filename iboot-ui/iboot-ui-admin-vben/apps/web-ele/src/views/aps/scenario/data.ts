import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsScenarioApi } from '#/api/aps/scenario';

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
      fieldName: 'code',
      label: '排程场景编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景编码',
      },
    },
    {
      fieldName: 'name',
      label: '排程场景名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景名称',
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
      label: '车间编号，0表示全工厂',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入车间编号，0表示全工厂',
      },
    },
    {
      fieldName: 'strategyId',
      label: '排程策略编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程策略编号',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划窗口开始时间',
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
      fieldName: 'planEndTime',
      label: '计划窗口结束时间',
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
      fieldName: 'snapshotTime',
      label: '输入数据快照时间',
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
      label: '状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED',
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
      label: 'APS-排程场景数据',
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
      fieldName: 'code',
      label: '排程场景编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景编码',
      },
    },
    {
      fieldName: 'name',
      label: '排程场景名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景名称',
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
      label: '车间编号，0表示全工厂',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入车间编号，0表示全工厂',
      },
    },
    {
      fieldName: 'strategyId',
      label: '排程策略编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程策略编号',
      },
    },
    {
      fieldName: 'planStartTime',
      label: '计划窗口开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'planEndTime',
      label: '计划窗口结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '输入数据快照时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED',
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
export function useGridColumns(): VxeTableGridOptions<ApsScenarioApi.Scenario>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '排程场景编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '排程场景名称',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'workshopId',
      title: '车间编号，0表示全工厂',
      minWidth: 120,
    },
    {
      field: 'strategyId',
      title: '排程策略编号',
      minWidth: 120,
    },
    {
      field: 'planStartTime',
      title: '计划窗口开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'planEndTime',
      title: '计划窗口结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '输入数据快照时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED',
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