import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsLocApi } from '#/api/wms/loc';

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
      fieldName: 'zoneId',
      label: '库区编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库区编号',
      },
    },
    {
      fieldName: 'code',
      label: '库位编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库位编码',
      },
    },
    {
      fieldName: 'locType',
      label: '库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX',
      },
    },
    {
      fieldName: 'aisleNo',
      label: '巷道号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入巷道号',
      },
    },
    {
      fieldName: 'rowNo',
      label: '排号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排号',
      },
    },
    {
      fieldName: 'colNo',
      label: '列号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入列号',
      },
    },
    {
      fieldName: 'levelNo',
      label: '层号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入层号',
      },
    },
    {
      fieldName: 'depthNo',
      label: '纵深号',
      component: 'Input',
      componentProps: {
        placeholder: '请输入纵深号',
      },
    },
    {
      fieldName: 'maxLength',
      label: '容器最大长度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器最大长度，单位毫米',
      },
    },
    {
      fieldName: 'maxWidth',
      label: '容器最大宽度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器最大宽度，单位毫米',
      },
    },
    {
      fieldName: 'maxHeight',
      label: '容器最大高度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器最大高度，单位毫米',
      },
    },
    {
      fieldName: 'maxWeight',
      label: '最大承重',
      component: 'Input',
      componentProps: {
        placeholder: '请输入最大承重',
      },
    },
    {
      fieldName: 'maxVolume',
      label: '最大容积',
      component: 'Input',
      componentProps: {
        placeholder: '请输入最大容积',
      },
    },
    {
      fieldName: 'maxContainerQty',
      label: '最大容器数量',
      component: 'Input',
      componentProps: {
        placeholder: '请输入最大容器数量',
      },
    },
    {
      fieldName: 'dimensionCheckMode',
      label: '尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM',
      },
    },
    {
      fieldName: 'status',
      label: '状态：EMPTY、OCCUPIED、LOCKED、DISABLED',
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
      label: 'WMS-库位数据',
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
      fieldName: 'zoneId',
      label: '库区编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库区编号',
      },
    },
    {
      fieldName: 'code',
      label: '库位编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位编码',
      },
    },
    {
      fieldName: 'locType',
      label: '库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX',
      },
    },
    {
      fieldName: 'aisleNo',
      label: '巷道号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入巷道号',
      },
    },
    {
      fieldName: 'rowNo',
      label: '排号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排号',
      },
    },
    {
      fieldName: 'colNo',
      label: '列号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入列号',
      },
    },
    {
      fieldName: 'levelNo',
      label: '层号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入层号',
      },
    },
    {
      fieldName: 'depthNo',
      label: '纵深号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入纵深号',
      },
    },
    {
      fieldName: 'maxLength',
      label: '容器最大长度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器最大长度，单位毫米',
      },
    },
    {
      fieldName: 'maxWidth',
      label: '容器最大宽度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器最大宽度，单位毫米',
      },
    },
    {
      fieldName: 'maxHeight',
      label: '容器最大高度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器最大高度，单位毫米',
      },
    },
    {
      fieldName: 'maxWeight',
      label: '最大承重',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入最大承重',
      },
    },
    {
      fieldName: 'maxVolume',
      label: '最大容积',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入最大容积',
      },
    },
    {
      fieldName: 'maxContainerQty',
      label: '最大容器数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入最大容器数量',
      },
    },
    {
      fieldName: 'dimensionCheckMode',
      label: '尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM',
      },
    },
    {
      fieldName: 'status',
      label: '状态：EMPTY、OCCUPIED、LOCKED、DISABLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：EMPTY、OCCUPIED、LOCKED、DISABLED',
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
export function useGridColumns(): VxeTableGridOptions<WmsLocApi.Loc>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '库位编码',
      minWidth: 120,
    },
    {
      field: 'locType',
      title: '库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX',
      minWidth: 120,
    },
    {
      field: 'aisleNo',
      title: '巷道号',
      minWidth: 120,
    },
    {
      field: 'rowNo',
      title: '排号',
      minWidth: 120,
    },
    {
      field: 'colNo',
      title: '列号',
      minWidth: 120,
    },
    {
      field: 'levelNo',
      title: '层号',
      minWidth: 120,
    },
    {
      field: 'depthNo',
      title: '纵深号',
      minWidth: 120,
    },
    {
      field: 'maxLength',
      title: '容器最大长度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'maxWidth',
      title: '容器最大宽度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'maxHeight',
      title: '容器最大高度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'maxWeight',
      title: '最大承重',
      minWidth: 120,
    },
    {
      field: 'maxVolume',
      title: '最大容积',
      minWidth: 120,
    },
    {
      field: 'maxContainerQty',
      title: '最大容器数量',
      minWidth: 120,
    },
    {
      field: 'dimensionCheckMode',
      title: '尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：EMPTY、OCCUPIED、LOCKED、DISABLED',
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
      field: 'zoneCode',
      title: '库区编码',
      minWidth: 120,
    },
    {
      field: 'zoneName',
      title: '库区名称',
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