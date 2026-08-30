import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDefectApi } from '#/api/mes/defect';

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
      fieldName: 'resultId',
      label: '关联检验结果编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入关联检验结果编号',
      },
    },
    {
      fieldName: 'defectCode',
      label: '缺陷编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷编码',
      },
    },
    {
      fieldName: 'defectName',
      label: '缺陷名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷名称',
      },
    },
    {
      fieldName: 'defectLevel',
      label: '缺陷等级：CRITICAL、MAJOR、MINOR',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷等级：CRITICAL、MAJOR、MINOR',
      },
    },
    {
      fieldName: 'qty',
      label: '缺陷数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷数量',
      },
    },
    {
      fieldName: 'description',
      label: '缺陷描述',
      component: 'RichTextarea',
    },
    {
      fieldName: 'location',
      label: '缺陷位置',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷位置',
      },
    },
    {
      fieldName: 'attachmentUrls',
      label: '缺陷附件地址列表',
      component: 'Input',
      componentProps: {
        placeholder: '请输入缺陷附件地址列表',
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
      label: 'MES-缺陷记录数据',
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
      fieldName: 'resultId',
      label: '关联检验结果编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入关联检验结果编号',
      },
    },
    {
      fieldName: 'defectCode',
      label: '缺陷编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷编码',
      },
    },
    {
      fieldName: 'defectName',
      label: '缺陷名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷名称',
      },
    },
    {
      fieldName: 'defectLevel',
      label: '缺陷等级：CRITICAL、MAJOR、MINOR',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷等级：CRITICAL、MAJOR、MINOR',
      },
    },
    {
      fieldName: 'qty',
      label: '缺陷数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷数量',
      },
    },
    {
      fieldName: 'description',
      label: '缺陷描述',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷描述',
      },
    },
    {
      fieldName: 'location',
      label: '缺陷位置',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷位置',
      },
    },
    {
      fieldName: 'attachmentUrls',
      label: '缺陷附件地址列表',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入缺陷附件地址列表',
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
export function useGridColumns(): VxeTableGridOptions<MesDefectApi.Defect>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'resultId',
      title: '关联检验结果编号',
      minWidth: 120,
    },
    {
      field: 'defectCode',
      title: '缺陷编码',
      minWidth: 120,
    },
    {
      field: 'defectName',
      title: '缺陷名称',
      minWidth: 120,
    },
    {
      field: 'defectLevel',
      title: '缺陷等级：CRITICAL、MAJOR、MINOR',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '缺陷数量',
      minWidth: 120,
    },
    {
      field: 'description',
      title: '缺陷描述',
      minWidth: 120,
    },
    {
      field: 'location',
      title: '缺陷位置',
      minWidth: 120,
    },
    {
      field: 'attachmentUrls',
      title: '缺陷附件地址列表',
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