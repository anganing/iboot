import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcResultApi } from '#/api/mes/qcresult';

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
      label: '质检任务编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入质检任务编号',
      },
    },
    {
      fieldName: 'itemId',
      label: '检验项目编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验项目编号',
      },
    },
    {
      fieldName: 'sampleNo',
      label: '样本序号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入样本序号',
      },
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验项编码快照',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称快照',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验项名称快照',
      },
    },
    {
      fieldName: 'actualValue',
      label: '实测值',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实测值',
      },
    },
    {
      fieldName: 'passFlag',
      label: '是否合格',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'inspectorId',
      label: '检验员编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验员编号',
      },
    },
    {
      fieldName: 'inspectTime',
      label: '检验时间',
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
      fieldName: 'qtyQualified',
      label: '本项判定合格数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入本项判定合格数量',
      },
    },
    {
      fieldName: 'qtyRejected',
      label: '本项判定不合格数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入本项判定不合格数量',
      },
    },
    {
      fieldName: 'conclusion',
      label: '项目结论：PASS、FAIL',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目结论：PASS、FAIL',
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
      label: 'MES-检验结果数据',
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
      label: '质检任务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入质检任务编号',
      },
    },
    {
      fieldName: 'itemId',
      label: '检验项目编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项目编号',
      },
    },
    {
      fieldName: 'sampleNo',
      label: '样本序号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入样本序号',
      },
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项编码快照',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项名称快照',
      },
    },
    {
      fieldName: 'actualValue',
      label: '实测值',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实测值',
      },
    },
    {
      fieldName: 'passFlag',
      label: '是否合格',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否合格',
      },
    },
    {
      fieldName: 'inspectorId',
      label: '检验员编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验员编号',
      },
    },
    {
      fieldName: 'inspectTime',
      label: '检验时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'qtyQualified',
      label: '本项判定合格数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入本项判定合格数量',
      },
    },
    {
      fieldName: 'qtyRejected',
      label: '本项判定不合格数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入本项判定不合格数量',
      },
    },
    {
      fieldName: 'conclusion',
      label: '项目结论：PASS、FAIL',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目结论：PASS、FAIL',
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
export function useGridColumns(): VxeTableGridOptions<MesQcResultApi.QcResult>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'itemId',
      title: '检验项目编号',
      minWidth: 120,
    },
    {
      field: 'sampleNo',
      title: '样本序号',
      minWidth: 120,
    },
    {
      field: 'itemCode',
      title: '检验项编码快照',
      minWidth: 120,
    },
    {
      field: 'itemName',
      title: '检验项名称快照',
      minWidth: 120,
    },
    {
      field: 'actualValue',
      title: '实测值',
      minWidth: 120,
    },
    {
      field: 'passFlag',
      title: '是否合格',
      minWidth: 120,
    },
    {
      field: 'inspectorId',
      title: '检验员编号',
      minWidth: 120,
    },
    {
      field: 'inspectTime',
      title: '检验时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'qtyQualified',
      title: '本项判定合格数量',
      minWidth: 120,
    },
    {
      field: 'qtyRejected',
      title: '本项判定不合格数量',
      minWidth: 120,
    },
    {
      field: 'conclusion',
      title: '项目结论：PASS、FAIL',
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