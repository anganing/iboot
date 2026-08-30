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
      label: '排程版本编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'fromTaskId',
      label: '前置排程任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入前置排程任务编号',
      },
    },
    {
      fieldName: 'toTaskId',
      label: '后置排程任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入后置排程任务编号',
      },
    },
    {
      fieldName: 'depType',
      label: '依赖类型：FINISH_START、START_START、FINISH_FINISH',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择依赖类型：FINISH_START、START_START、FINISH_FINISH',
      },
    },
    {
      fieldName: 'lagMinutes',
      label: '依赖间隔分钟数',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入依赖间隔分钟数',
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
      label: 'APS-排程任务依赖数据',
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
      label: '排程版本编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本编号',
      },
    },
    {
      fieldName: 'fromTaskId',
      label: '前置排程任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入前置排程任务编号',
      },
    },
    {
      fieldName: 'toTaskId',
      label: '后置排程任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入后置排程任务编号',
      },
    },
    {
      fieldName: 'depType',
      label: '依赖类型：FINISH_START、START_START、FINISH_FINISH',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择依赖类型：FINISH_START、START_START、FINISH_FINISH',
      },
    },
    {
      fieldName: 'lagMinutes',
      label: '依赖间隔分钟数',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入依赖间隔分钟数',
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
      title: '前置排程任务编号',
      minWidth: 120,
    },
    {
      field: 'toTaskId',
      title: '后置排程任务编号',
      minWidth: 120,
    },
    {
      field: 'depType',
      title: '依赖类型：FINISH_START、START_START、FINISH_FINISH',
      minWidth: 120,
    },
    {
      field: 'lagMinutes',
      title: '依赖间隔分钟数',
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
      title: '排程版本version_no',
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