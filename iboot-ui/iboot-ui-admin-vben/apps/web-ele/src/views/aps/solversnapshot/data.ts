import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsSolverSnapshotApi } from '#/api/aps/solversnapshot';

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
      fieldName: 'snapshotType',
      label: '快照类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择快照类型',
      },
    },
    {
      fieldName: 'snapshotJson',
      label: '求解内容',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入求解内容',
      },
    },
    {
      fieldName: 'checksum',
      label: '内容校验值',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入内容校验值',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '快照生成时间',
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
      label: '求解快照数据',
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
      fieldName: 'snapshotType',
      label: '快照类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择快照类型',
      },
    },
    {
      fieldName: 'snapshotJson',
      label: '求解内容',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入求解内容',
      },
    },
    {
      fieldName: 'checksum',
      label: '内容校验值',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入内容校验值',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '快照生成时间',
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
export function useGridColumns(): VxeTableGridOptions<ApsSolverSnapshotApi.SolverSnapshot>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'snapshotType',
      title: '快照类型',
      minWidth: 120,
    },
    {
      field: 'snapshotJson',
      title: '求解内容',
      minWidth: 120,
    },
    {
      field: 'checksum',
      title: '内容校验值',
      minWidth: 120,
    },
    {
      field: 'snapshotTime',
      title: '快照生成时间',
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