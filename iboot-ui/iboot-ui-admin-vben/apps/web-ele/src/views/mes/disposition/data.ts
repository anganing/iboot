import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispositionApi } from '#/api/mes/disposition';

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
      fieldName: 'dispositionNo',
      label: '不合格处置单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入不合格处置单号',
      },
    },
    {
      fieldName: 'taskId',
      label: '质检任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检任务编号',
      },
    },
    {
      fieldName: 'dispositionType',
      label: '处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS',
      },
    },
    {
      fieldName: 'qty',
      label: '处置数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入处置数量',
      },
    },
    {
      fieldName: 'targetBizType',
      label: '联动目标业务类型',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择联动目标业务类型',
      },
    },
    {
      fieldName: 'targetBizNo',
      label: '联动目标业务单号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入联动目标业务单号',
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
      fieldName: 'approveTime',
      label: '审批时间',
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
      label: '状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED',
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
      label: '处置幂等键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入处置幂等键',
      },
    },
    {
      fieldName: 'remark',
      label: '处置说明',
      component: 'Input',
      componentProps: {
        placeholder: '请输入处置说明',
      },
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: 'MES-不合格品处置单数据',
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
      fieldName: 'dispositionNo',
      label: '不合格处置单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入不合格处置单号',
      },
    },
    {
      fieldName: 'taskId',
      label: '质检任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检任务编号',
      },
    },
    {
      fieldName: 'dispositionType',
      label: '处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS',
      },
    },
    {
      fieldName: 'qty',
      label: '处置数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入处置数量',
      },
    },
    {
      fieldName: 'targetBizType',
      label: '联动目标业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择联动目标业务类型',
      },
    },
    {
      fieldName: 'targetBizNo',
      label: '联动目标业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入联动目标业务单号',
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
      fieldName: 'approveTime',
      label: '审批时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '处置幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入处置幂等键',
      },
    },
    {
      fieldName: 'remark',
      label: '处置说明',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入处置说明',
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
export function useGridColumns(): VxeTableGridOptions<MesDispositionApi.Disposition>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'dispositionNo',
      title: '不合格处置单号',
      minWidth: 120,
    },
    {
      field: 'dispositionType',
      title: '处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '处置数量',
      minWidth: 120,
    },
    {
      field: 'targetBizType',
      title: '联动目标业务类型',
      minWidth: 120,
    },
    {
      field: 'targetBizNo',
      title: '联动目标业务单号',
      minWidth: 120,
    },
    {
      field: 'approvedBy',
      title: '审批人员编号',
      minWidth: 120,
    },
    {
      field: 'approveTime',
      title: '审批时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '处置幂等键',
      minWidth: 120,
    },
    {
      field: 'remark',
      title: '处置说明',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'taskTask_no',
      title: '质检任务task_no',
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