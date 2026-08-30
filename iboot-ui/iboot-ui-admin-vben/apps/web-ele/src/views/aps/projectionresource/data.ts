import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionResourceApi } from '#/api/aps/projectionresource';

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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceResId',
      label: 'MDM生产资源编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入MDM生产资源编号',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源编码快照',
      },
    },
    {
      fieldName: 'resName',
      label: '资源名称快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源名称快照',
      },
    },
    {
      fieldName: 'resType',
      label: '资源类型快照',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择资源类型快照',
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
      fieldName: 'wcId',
      label: '工作中心编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'workDate',
      label: '产能日期',
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
      fieldName: 'shiftId',
      label: '班次编号，0表示日汇总',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入班次编号，0表示日汇总',
      },
    },
    {
      fieldName: 'availableMinutes',
      label: '可用分钟数',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入可用分钟数',
      },
    },
    {
      fieldName: 'capabilityJson',
      label: '资源能力快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入资源能力快照',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
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
      label: '投影快照时间',
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
      label: 'APS-MDM资源与日历投影数据',
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
      fieldName: 'scenarioId',
      label: '排程场景编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景编号',
      },
    },
    {
      fieldName: 'sourceResId',
      label: 'MDM生产资源编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入MDM生产资源编号',
      },
    },
    {
      fieldName: 'resCode',
      label: '资源编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源编码快照',
      },
    },
    {
      fieldName: 'resName',
      label: '资源名称快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源名称快照',
      },
    },
    {
      fieldName: 'resType',
      label: '资源类型快照',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择资源类型快照',
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
      fieldName: 'wcId',
      label: '工作中心编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工作中心编号',
      },
    },
    {
      fieldName: 'workDate',
      label: '产能日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'shiftId',
      label: '班次编号，0表示日汇总',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入班次编号，0表示日汇总',
      },
    },
    {
      fieldName: 'availableMinutes',
      label: '可用分钟数',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入可用分钟数',
      },
    },
    {
      fieldName: 'capabilityJson',
      label: '资源能力快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入资源能力快照',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<ApsProjectionResourceApi.ProjectionResource>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'sourceResId',
      title: 'MDM生产资源编号',
      minWidth: 120,
    },
    {
      field: 'resCode',
      title: '资源编码快照',
      minWidth: 120,
    },
    {
      field: 'resName',
      title: '资源名称快照',
      minWidth: 120,
    },
    {
      field: 'resType',
      title: '资源类型快照',
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
      field: 'wcId',
      title: '工作中心编号',
      minWidth: 120,
    },
    {
      field: 'workDate',
      title: '产能日期',
      minWidth: 120,
    },
    {
      field: 'shiftId',
      title: '班次编号，0表示日汇总',
      minWidth: 120,
    },
    {
      field: 'availableMinutes',
      title: '可用分钟数',
      minWidth: 120,
    },
    {
      field: 'capabilityJson',
      title: '资源能力快照',
      minWidth: 120,
    },
    {
      field: 'sourceUpdateTime',
      title: '来源数据更新时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '投影快照时间',
      minWidth: 120,
      formatter: 'formatDateTime',
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
      field: 'scenarioCode',
      title: '排程场景编码',
      minWidth: 120,
    },
    {
      field: 'scenarioName',
      title: '排程场景名称',
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