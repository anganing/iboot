import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesQcItemApi } from '#/api/mes/qcitem';

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
      fieldName: 'planId',
      label: '检验方案编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验方案编号',
      },
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验项编码',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验项名称',
      },
    },
    {
      fieldName: 'itemType',
      label: '检验项类型：NUMERIC、TEXT、BOOLEAN',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择检验项类型：NUMERIC、TEXT、BOOLEAN',
      },
    },
    {
      fieldName: 'unit',
      label: '检验值单位',
      component: 'Input',
      componentProps: {
        placeholder: '请输入检验值单位',
      },
    },
    {
      fieldName: 'stdValue',
      label: '标准值',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标准值',
      },
    },
    {
      fieldName: 'upperLimit',
      label: '数值上限',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数值上限',
      },
    },
    {
      fieldName: 'lowerLimit',
      label: '数值下限',
      component: 'Input',
      componentProps: {
        placeholder: '请输入数值下限',
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必检',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'sortNo',
      label: '排序号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排序号',
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
      label: 'MES-检验项目数据',
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
      fieldName: 'planId',
      label: '检验方案编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验方案编号',
      },
    },
    {
      fieldName: 'itemCode',
      label: '检验项编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项编码',
      },
    },
    {
      fieldName: 'itemName',
      label: '检验项名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验项名称',
      },
    },
    {
      fieldName: 'itemType',
      label: '检验项类型：NUMERIC、TEXT、BOOLEAN',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择检验项类型：NUMERIC、TEXT、BOOLEAN',
      },
    },
    {
      fieldName: 'unit',
      label: '检验值单位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检验值单位',
      },
    },
    {
      fieldName: 'stdValue',
      label: '标准值',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入标准值',
      },
    },
    {
      fieldName: 'upperLimit',
      label: '数值上限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入数值上限',
      },
    },
    {
      fieldName: 'lowerLimit',
      label: '数值下限',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入数值下限',
      },
    },
    {
      fieldName: 'requiredFlag',
      label: '是否必检',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否必检',
      },
    },
    {
      fieldName: 'sortNo',
      label: '排序号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排序号',
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
export function useGridColumns(): VxeTableGridOptions<MesQcItemApi.QcItem>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'itemCode',
      title: '检验项编码',
      minWidth: 120,
    },
    {
      field: 'itemName',
      title: '检验项名称',
      minWidth: 120,
    },
    {
      field: 'itemType',
      title: '检验项类型：NUMERIC、TEXT、BOOLEAN',
      minWidth: 120,
    },
    {
      field: 'unit',
      title: '检验值单位',
      minWidth: 120,
    },
    {
      field: 'stdValue',
      title: '标准值',
      minWidth: 120,
    },
    {
      field: 'upperLimit',
      title: '数值上限',
      minWidth: 120,
    },
    {
      field: 'lowerLimit',
      title: '数值下限',
      minWidth: 120,
    },
    {
      field: 'requiredFlag',
      title: '是否必检',
      minWidth: 120,
    },
    {
      field: 'sortNo',
      title: '排序号',
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
      field: 'planCode',
      title: '检验方案编码',
      minWidth: 120,
    },
    {
      field: 'planName',
      title: '检验方案名称',
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