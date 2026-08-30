import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskDepApi } from '#/api/aps/taskdep';

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
      fieldName: 'versionId',
      label: '排程版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'fromTaskId',
      label: '前置排程任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入前置排程任务',
      },
    },
    {
      fieldName: 'toTaskId',
      label: '后置排程任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入后置排程任务',
      },
    },
    {
      fieldName: 'depType',
      label: '依赖类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择依赖类型',
      },
    },
    {
      fieldName: 'lagMinutes',
      label: '间隔(分钟)',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入间隔(分钟)',
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
      label: '任务依赖数据',
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
      fieldName: 'versionId',
      label: '排程版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本',
      },
    },
    {
      fieldName: 'fromTaskId',
      label: '前置排程任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入前置排程任务',
      },
    },
    {
      fieldName: 'toTaskId',
      label: '后置排程任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入后置排程任务',
      },
    },
    {
      fieldName: 'depType',
      label: '依赖类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择依赖类型',
      },
    },
    {
      fieldName: 'lagMinutes',
      label: '间隔(分钟)',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入间隔(分钟)',
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
export function useGridColumns(): VxeTableGridOptions<ApsTaskDepApi.TaskDep>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'fromTaskId',
      title: '前置排程任务',
      minWidth: 120,
    },
    {
      field: 'toTaskId',
      title: '后置排程任务',
      minWidth: 120,
    },
    {
      field: 'depType',
      title: '依赖类型',
      minWidth: 120,
    },
    {
      field: 'lagMinutes',
      title: '间隔(分钟)',
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
      field: 'versionVersion_no',
      title: '排程版本号',
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