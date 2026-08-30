import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcTaskApi } from '#/api/mes/qctask';

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
      fieldName: 'taskNo',
      label: '质检任务号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检任务号',
      },
    },
    {
      fieldName: 'ruleId',
      label: '命中的质检规则编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入命中的质检规则编号',
      },
    },
    {
      fieldName: 'qcPlanId',
      label: '锁定的检验方案编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的检验方案编号',
      },
    },
    {
      fieldName: 'qcPlanVersion',
      label: '锁定的检验方案版本',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入锁定的检验方案版本',
      },
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点：IQC、IPQC、FQC、OQC、FAI',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入触发点：IQC、IPQC、FQC、OQC、FAI',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'bizLineId',
      label: '来源业务明细编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入来源业务明细编号',
      },
    },
    {
      fieldName: 'matId',
      label: '检验物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验物料编号',
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
      fieldName: 'batchId',
      label: '检验批次编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验批次编号',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次号快照',
      },
    },
    {
      fieldName: 'qty',
      label: '送检数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入送检数量',
      },
    },
    {
      fieldName: 'sampleQty',
      label: '抽样数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入抽样数量',
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
      fieldName: 'assignedInspectorId',
      label: '指派检验员编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入指派检验员编号',
      },
    },
    {
      fieldName: 'inspectStartTime',
      label: '检验开始时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'inspectEndTime',
      label: '检验结束时间',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'conclusion',
      label: '检验结论：QUALIFIED、REJECTED、CONCESSION',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验结论：QUALIFIED、REJECTED、CONCESSION',
      },
    },
    {
      fieldName: 'qtyQualified',
      label: '合格数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入合格数量',
      },
    },
    {
      fieldName: 'qtyRejected',
      label: '不合格数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入不合格数量',
      },
    },
    {
      fieldName: 'status',
      label: '状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED',
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
      label: '创建幂等键',
      component: 'Input',
      componentProps: {
        placeholder: '请输入创建幂等键',
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
      label: 'MES-质检任务数据',
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
      fieldName: 'taskNo',
      label: '质检任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检任务号',
      },
    },
    {
      fieldName: 'ruleId',
      label: '命中的质检规则编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入命中的质检规则编号',
      },
    },
    {
      fieldName: 'qcPlanId',
      label: '锁定的检验方案编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的检验方案编号',
      },
    },
    {
      fieldName: 'qcPlanVersion',
      label: '锁定的检验方案版本',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入锁定的检验方案版本',
      },
    },
    {
      fieldName: 'triggerPoint',
      label: '触发点：IQC、IPQC、FQC、OQC、FAI',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入触发点：IQC、IPQC、FQC、OQC、FAI',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择来源业务类型',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'bizLineId',
      label: '来源业务明细编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务明细编号',
      },
    },
    {
      fieldName: 'matId',
      label: '检验物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验物料编号',
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
      fieldName: 'batchId',
      label: '检验批次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验批次编号',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号快照',
      },
    },
    {
      fieldName: 'qty',
      label: '送检数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入送检数量',
      },
    },
    {
      fieldName: 'sampleQty',
      label: '抽样数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入抽样数量',
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
      fieldName: 'assignedInspectorId',
      label: '指派检验员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入指派检验员编号',
      },
    },
    {
      fieldName: 'inspectStartTime',
      label: '检验开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'inspectEndTime',
      label: '检验结束时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'conclusion',
      label: '检验结论：QUALIFIED、REJECTED、CONCESSION',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验结论：QUALIFIED、REJECTED、CONCESSION',
      },
    },
    {
      fieldName: 'qtyQualified',
      label: '合格数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入合格数量',
      },
    },
    {
      fieldName: 'qtyRejected',
      label: '不合格数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入不合格数量',
      },
    },
    {
      fieldName: 'status',
      label: '状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '创建幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入创建幂等键',
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
export function useGridColumns(): VxeTableGridOptions<MesQcTaskApi.QcTask>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'taskNo',
      title: '质检任务号',
      minWidth: 120,
    },
    {
      field: 'qcPlanVersion',
      title: '锁定的检验方案版本',
      minWidth: 120,
    },
    {
      field: 'triggerPoint',
      title: '触发点：IQC、IPQC、FQC、OQC、FAI',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '来源业务类型',
      minWidth: 120,
    },
    {
      field: 'bizNo',
      title: '来源业务单号',
      minWidth: 120,
    },
    {
      field: 'bizLineId',
      title: '来源业务明细编号',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '检验物料编号',
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
      field: 'batchId',
      title: '检验批次编号',
      minWidth: 120,
    },
    {
      field: 'lotNo',
      title: '批次号快照',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '送检数量',
      minWidth: 120,
    },
    {
      field: 'sampleQty',
      title: '抽样数量',
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
      field: 'assignedInspectorId',
      title: '指派检验员编号',
      minWidth: 120,
    },
    {
      field: 'inspectStartTime',
      title: '检验开始时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'inspectEndTime',
      title: '检验结束时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'conclusion',
      title: '检验结论：QUALIFIED、REJECTED、CONCESSION',
      minWidth: 120,
    },
    {
      field: 'qtyQualified',
      title: '合格数量',
      minWidth: 120,
    },
    {
      field: 'qtyRejected',
      title: '不合格数量',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '创建幂等键',
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
      field: 'ruleCode',
      title: '命中的质检规则编码',
      minWidth: 120,
    },
    {
      field: 'ruleName',
      title: '命中的质检规则名称',
      minWidth: 120,
    },
    {
      field: 'qcPlanCode',
      title: '锁定的检验方案编码',
      minWidth: 120,
    },
    {
      field: 'qcPlanName',
      title: '锁定的检验方案名称',
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