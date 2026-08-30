import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsZoneApi } from '#/api/wms/zone';

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
      fieldName: 'whId',
      label: '仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'code',
      label: '库区编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库区编码',
      },
    },
    {
      fieldName: 'name',
      label: '库区名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库区名称',
      },
    },
    {
      fieldName: 'zoneType',
      label: '库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING',
      },
    },
    {
      fieldName: 'allowMixedMat',
      label: '是否允许混放物料',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'allowMixedLot',
      label: '是否允许混放批次',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'tempRange',
      label: '温区要求',
      component: 'Input',
      componentProps: {
        placeholder: '请输入温区要求',
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
      label: 'WMS-库区数据',
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
      fieldName: 'whId',
      label: '仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'code',
      label: '库区编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区编码',
      },
    },
    {
      fieldName: 'name',
      label: '库区名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区名称',
      },
    },
    {
      fieldName: 'zoneType',
      label: '库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING',
      },
    },
    {
      fieldName: 'allowMixedMat',
      label: '是否允许混放物料',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否允许混放物料',
      },
    },
    {
      fieldName: 'allowMixedLot',
      label: '是否允许混放批次',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择是否允许混放批次',
      },
    },
    {
      fieldName: 'tempRange',
      label: '温区要求',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入温区要求',
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
export function useGridColumns(): VxeTableGridOptions<WmsZoneApi.Zone>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '库区编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '库区名称',
      minWidth: 120,
    },
    {
      field: 'zoneType',
      title: '库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING',
      minWidth: 120,
    },
    {
      field: 'allowMixedMat',
      title: '是否允许混放物料',
      minWidth: 120,
    },
    {
      field: 'allowMixedLot',
      title: '是否允许混放批次',
      minWidth: 120,
    },
    {
      field: 'tempRange',
      title: '温区要求',
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
      field: 'whCode',
      title: '仓库编码',
      minWidth: 120,
    },
    {
      field: 'whName',
      title: '仓库名称',
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