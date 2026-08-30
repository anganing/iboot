import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskLockApi } from '#/api/aps/tasklock';

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
      fieldName: 'taskId',
      label: '排程任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程任务',
      },
    },
    {
      fieldName: 'lockType',
      label: '锁定类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择锁定类型',
      },
    },
    {
      fieldName: 'reason',
      label: '锁定原因',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定原因',
      },
    },
    {
      fieldName: 'lockedBy',
      label: '锁定人员',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定人员',
      },
    },
    {
      fieldName: 'lockedTime',
      label: '锁定时间',
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
      fieldName: 'unlockTime',
      label: '解锁时间',
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
      label: '状态',
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
      label: '任务锁定数据',
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
      fieldName: 'taskId',
      label: '排程任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程任务',
      },
    },
    {
      fieldName: 'lockType',
      label: '锁定类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择锁定类型',
      },
    },
    {
      fieldName: 'reason',
      label: '锁定原因',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定原因',
      },
    },
    {
      fieldName: 'lockedBy',
      label: '锁定人员',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定人员',
      },
    },
    {
      fieldName: 'lockedTime',
      label: '锁定时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'unlockTime',
      label: '解锁时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态',
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
export function useGridColumns(): VxeTableGridOptions<ApsTaskLockApi.TaskLock>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskId',
      title: '排程任务',
      minWidth: 120,
    },
    {
      field: 'lockType',
      title: '锁定类型',
      minWidth: 120,
    },
    {
      field: 'reason',
      title: '锁定原因',
      minWidth: 120,
    },
    {
      field: 'lockedBy',
      title: '锁定人员',
      minWidth: 120,
    },
    {
      field: 'lockedTime',
      title: '锁定时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'unlockTime',
      title: '解锁时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
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