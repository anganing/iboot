import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpSkipRecordApi } from '#/api/mes/opskiprecord';

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
      fieldName: 'opTaskId',
      label: '被跳过工序任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入被跳过工序任务编号',
      },
    },
    {
      fieldName: 'skipReasonType',
      label: '跳过原因类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择跳过原因类型',
      },
    },
    {
      fieldName: 'skipReason',
      label: '跳过原因',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入跳过原因',
      },
    },
    {
      fieldName: 'operatorId',
      label: '操作人员编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入操作人员编号',
      },
    },
    {
      fieldName: 'approvedBy',
      label: '审批人员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入审批人员编号',
      },
    },
    {
      fieldName: 'approveStatus',
      label: '审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'skipTime',
      label: '实际跳过时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'effectRemark',
      label: '对后续工序、质检及排程的影响说明',
      component: 'Input',
      componentProps: {
        placeholder: '请输入对后续工序、质检及排程的影响说明',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、EFFECTIVE、REVOKED',
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
      label: 'MES-工序跳过记录数据',
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
      fieldName: 'opTaskId',
      label: '被跳过工序任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入被跳过工序任务编号',
      },
    },
    {
      fieldName: 'skipReasonType',
      label: '跳过原因类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择跳过原因类型',
      },
    },
    {
      fieldName: 'skipReason',
      label: '跳过原因',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入跳过原因',
      },
    },
    {
      fieldName: 'operatorId',
      label: '操作人员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入操作人员编号',
      },
    },
    {
      fieldName: 'approvedBy',
      label: '审批人员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入审批人员编号',
      },
    },
    {
      fieldName: 'approveStatus',
      label: '审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED',
      },
    },
    {
      fieldName: 'skipTime',
      label: '实际跳过时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'effectRemark',
      label: '对后续工序、质检及排程的影响说明',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入对后续工序、质检及排程的影响说明',
      },
    },
    {
      fieldName: 'status',
      label: '状态：PENDING、EFFECTIVE、REVOKED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：PENDING、EFFECTIVE、REVOKED',
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
export function useGridColumns(): VxeTableGridOptions<MesOpSkipRecordApi.OpSkipRecord>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'skipReasonType',
      title: '跳过原因类型',
      minWidth: 120,
    },
    {
      field: 'skipReason',
      title: '跳过原因',
      minWidth: 120,
    },
    {
      field: 'operatorId',
      title: '操作人员编号',
      minWidth: 120,
    },
    {
      field: 'approvedBy',
      title: '审批人员编号',
      minWidth: 120,
    },
    {
      field: 'approveStatus',
      title: '审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED',
      minWidth: 120,
    },
    {
      field: 'skipTime',
      title: '实际跳过时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'effectRemark',
      title: '对后续工序、质检及排程的影响说明',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：PENDING、EFFECTIVE、REVOKED',
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
      field: 'opTaskTask_no',
      title: '被跳过工序任务task_no',
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