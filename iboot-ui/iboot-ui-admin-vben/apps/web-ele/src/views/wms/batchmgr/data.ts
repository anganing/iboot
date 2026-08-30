import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsBatchApi } from '#/api/wms/batchmgr';

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
      fieldName: 'matId',
      label: '物料编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编号',
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
      fieldName: 'lotNo',
      label: '批次号；无批次物料使用默认批次',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次号；无批次物料使用默认批次',
      },
    },
    {
      fieldName: 'snNo',
      label: '序列号；SN物料以数量为1的批次承载',
      component: 'Input',
      componentProps: {
        placeholder: '请输入序列号；SN物料以数量为1的批次承载',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商编号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商编号',
      },
    },
    {
      fieldName: 'supplierCode',
      label: '供应商编码快照',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商编码快照',
      },
    },
    {
      fieldName: 'supplierLotNo',
      label: '供应商批次号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入供应商批次号',
      },
    },
    {
      fieldName: 'furnaceLotNo',
      label: '炉批号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入炉批号',
      },
    },
    {
      fieldName: 'produceDate',
      label: '生产日期',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
        class: '!w-full',
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
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
      label: '状态：0-启用，1-停用',
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
      label: 'WMS-批次主档数据',
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
      fieldName: 'matId',
      label: '物料编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编号',
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
      fieldName: 'lotNo',
      label: '批次号；无批次物料使用默认批次',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号；无批次物料使用默认批次',
      },
    },
    {
      fieldName: 'snNo',
      label: '序列号；SN物料以数量为1的批次承载',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入序列号；SN物料以数量为1的批次承载',
      },
    },
    {
      fieldName: 'supplierId',
      label: '供应商编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商编号',
      },
    },
    {
      fieldName: 'supplierCode',
      label: '供应商编码快照',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商编码快照',
      },
    },
    {
      fieldName: 'supplierLotNo',
      label: '供应商批次号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入供应商批次号',
      },
    },
    {
      fieldName: 'furnaceLotNo',
      label: '炉批号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入炉批号',
      },
    },
    {
      fieldName: 'produceDate',
      label: '生产日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'expiryDate',
      label: '失效日期',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '状态：0-启用，1-停用',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：0-启用，1-停用',
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
export function useGridColumns(): VxeTableGridOptions<WmsBatchApi.Batch>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'matId',
      title: '物料编号',
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
      field: 'lotNo',
      title: '批次号；无批次物料使用默认批次',
      minWidth: 120,
    },
    {
      field: 'snNo',
      title: '序列号；SN物料以数量为1的批次承载',
      minWidth: 120,
    },
    {
      field: 'supplierId',
      title: '供应商编号',
      minWidth: 120,
    },
    {
      field: 'supplierCode',
      title: '供应商编码快照',
      minWidth: 120,
    },
    {
      field: 'supplierLotNo',
      title: '供应商批次号',
      minWidth: 120,
    },
    {
      field: 'furnaceLotNo',
      title: '炉批号',
      minWidth: 120,
    },
    {
      field: 'produceDate',
      title: '生产日期',
      minWidth: 120,
    },
    {
      field: 'expiryDate',
      title: '失效日期',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：0-启用，1-停用',
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