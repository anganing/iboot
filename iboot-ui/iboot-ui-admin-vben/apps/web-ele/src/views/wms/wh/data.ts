import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsWhApi } from '#/api/wms/wh';

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
      fieldName: 'invOrgId',
      label: '库存组织编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'code',
      label: '仓库编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编码',
      },
    },
    {
      fieldName: 'name',
      label: '仓库名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库名称',
      },
    },
    {
      fieldName: 'whType',
      label: '仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: [],
        placeholder: '请选择仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓',
      },
    },
    {
      fieldName: 'managedBy',
      label: '管理方式：MANUAL、WCS、AGV、MIXED',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入管理方式：MANUAL、WCS、AGV、MIXED',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部设备系统编码',
      component: 'Input',
      componentProps: {
        placeholder: '请输入外部设备系统编码',
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
      label: 'WMS-仓库数据',
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
      fieldName: 'invOrgId',
      label: '库存组织编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存组织编号',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂编号',
      },
    },
    {
      fieldName: 'code',
      label: '仓库编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编码',
      },
    },
    {
      fieldName: 'name',
      label: '仓库名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库名称',
      },
    },
    {
      fieldName: 'whType',
      label: '仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓',
      },
    },
    {
      fieldName: 'managedBy',
      label: '管理方式：MANUAL、WCS、AGV、MIXED',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入管理方式：MANUAL、WCS、AGV、MIXED',
      },
    },
    {
      fieldName: 'externalSystemCode',
      label: '外部设备系统编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入外部设备系统编码',
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
export function useGridColumns(): VxeTableGridOptions<WmsWhApi.Wh>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂编号',
      minWidth: 120,
    },
    {
      field: 'code',
      title: '仓库编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '仓库名称',
      minWidth: 120,
    },
    {
      field: 'whType',
      title: '仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓',
      minWidth: 120,
    },
    {
      field: 'managedBy',
      title: '管理方式：MANUAL、WCS、AGV、MIXED',
      minWidth: 120,
    },
    {
      field: 'externalSystemCode',
      title: '外部设备系统编码',
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