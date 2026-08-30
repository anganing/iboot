import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsContainerApi } from '#/api/wms/containermgr';

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
      fieldName: 'containerNo',
      label: '容器编号或条码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器编号或条码',
      },
    },
    {
      fieldName: 'typeId',
      label: '容器类型编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器类型编号',
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
      fieldName: 'whId',
      label: '当前仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '当前库位编号，0表示未落位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入当前库位编号，0表示未落位',
      },
    },
    {
      fieldName: 'length',
      label: '实际长度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实际长度，单位毫米',
      },
    },
    {
      fieldName: 'width',
      label: '实际宽度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实际宽度，单位毫米',
      },
    },
    {
      fieldName: 'height',
      label: '实际高度，单位毫米',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实际高度，单位毫米',
      },
    },
    {
      fieldName: 'grossWeight',
      label: '实际毛重',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实际毛重',
      },
    },
    {
      fieldName: 'status',
      label: '状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
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
      label: 'WMS-容器主档数据',
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
      fieldName: 'containerNo',
      label: '容器编号或条码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器编号或条码',
      },
    },
    {
      fieldName: 'typeId',
      label: '容器类型编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器类型编号',
      },
    },
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
      fieldName: 'whId',
      label: '当前仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '当前库位编号，0表示未落位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入当前库位编号，0表示未落位',
      },
    },
    {
      fieldName: 'length',
      label: '实际长度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实际长度，单位毫米',
      },
    },
    {
      fieldName: 'width',
      label: '实际宽度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实际宽度，单位毫米',
      },
    },
    {
      fieldName: 'height',
      label: '实际高度，单位毫米',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实际高度，单位毫米',
      },
    },
    {
      fieldName: 'grossWeight',
      label: '实际毛重',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入实际毛重',
      },
    },
    {
      fieldName: 'status',
      label: '状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED',
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
export function useGridColumns(): VxeTableGridOptions<WmsContainerApi.Container>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'containerNo',
      title: '容器编号或条码',
      minWidth: 120,
    },
    {
      field: 'typeId',
      title: '容器类型编号',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'locId',
      title: '当前库位编号，0表示未落位',
      minWidth: 120,
    },
    {
      field: 'length',
      title: '实际长度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'width',
      title: '实际宽度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'height',
      title: '实际高度，单位毫米',
      minWidth: 120,
    },
    {
      field: 'grossWeight',
      title: '实际毛重',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED',
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
      field: 'whCode',
      title: '当前仓库编码',
      minWidth: 120,
    },
    {
      field: 'whName',
      title: '当前仓库名称',
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