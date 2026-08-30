import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsVersionApi } from '#/api/aps/version';

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
      fieldName: 'versionNo',
      label: '排程版本号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'score',
      label: 'Timefold综合得分',
      component: 'Input',
      componentProps: {
        placeholder: '请输入Timefold综合得分',
      },
    },
    {
      fieldName: 'hardScore',
      label: '硬约束得分',
      component: 'Input',
      componentProps: {
        placeholder: '请输入硬约束得分',
      },
    },
    {
      fieldName: 'softScore',
      label: '软约束得分',
      component: 'Input',
      componentProps: {
        placeholder: '请输入软约束得分',
      },
    },
    {
      fieldName: 'solveStartTime',
      label: '求解开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'solveEndTime',
      label: '求解结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'publishedTime',
      label: '发布时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'publishedBy',
      label: '发布人员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入发布人员编号',
      },
    },
    {
      fieldName: 'solverMessage',
      label: '求解结果或失败信息',
      component: 'Input',
      componentProps: {
        placeholder: '请输入求解结果或失败信息',
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
      label: 'APS-排程版本数据',
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
      fieldName: 'versionNo',
      label: '排程版本号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程版本号',
      },
    },
    {
      fieldName: 'status',
      label: '状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED',
      },
    },
    {
      fieldName: 'score',
      label: 'Timefold综合得分',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入Timefold综合得分',
      },
    },
    {
      fieldName: 'hardScore',
      label: '硬约束得分',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入硬约束得分',
      },
    },
    {
      fieldName: 'softScore',
      label: '软约束得分',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入软约束得分',
      },
    },
    {
      fieldName: 'solveStartTime',
      label: '求解开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'solveEndTime',
      label: '求解结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'publishedTime',
      label: '发布时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'publishedBy',
      label: '发布人员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入发布人员编号',
      },
    },
    {
      fieldName: 'solverMessage',
      label: '求解结果或失败信息',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入求解结果或失败信息',
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
export function useGridColumns(): VxeTableGridOptions<ApsVersionApi.Version>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'versionNo',
      title: '排程版本号',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED',
      minWidth: 120,
    },
    {
      field: 'score',
      title: 'Timefold综合得分',
      minWidth: 120,
    },
    {
      field: 'hardScore',
      title: '硬约束得分',
      minWidth: 120,
    },
    {
      field: 'softScore',
      title: '软约束得分',
      minWidth: 120,
    },
    {
      field: 'solveStartTime',
      title: '求解开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'solveEndTime',
      title: '求解结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'publishedTime',
      title: '发布时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'publishedBy',
      title: '发布人员编号',
      minWidth: 120,
    },
    {
      field: 'solverMessage',
      title: '求解结果或失败信息',
      minWidth: 120,
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