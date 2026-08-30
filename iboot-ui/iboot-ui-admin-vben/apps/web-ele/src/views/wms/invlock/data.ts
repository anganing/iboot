import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvLockApi } from '#/api/wms/invlock';

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
      fieldName: 'lockNo',
      label: '冻结编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结编号',
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
      label: '仓库编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '库位编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库位编号',
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
      fieldName: 'batchId',
      label: '批次编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次编号',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示散件',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入容器编号，0表示散件',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: [],
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'qty',
      label: '冻结数量，基准单位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结数量，基准单位',
      },
    },
    {
      fieldName: 'releasedQty',
      label: '已解冻数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入已解冻数量',
      },
    },
    {
      fieldName: 'reason',
      label: '冻结原因',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结原因',
      },
    },
    {
      fieldName: 'status',
      label: '状态：ACTIVE、PARTIAL、RELEASED、CANCELLED',
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
      label: '冻结幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结幂等键',
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
      label: 'WMS-库存冻结记录数据',
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
      fieldName: 'lockNo',
      label: '冻结编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结编号',
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
      label: '仓库编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库编号',
      },
    },
    {
      fieldName: 'locId',
      label: '库位编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位编号',
      },
    },
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
      fieldName: 'batchId',
      label: '批次编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次编号',
      },
    },
    {
      fieldName: 'containerId',
      label: '容器编号，0表示散件',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入容器编号，0表示散件',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目编号，0表示无项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目编号，0表示无项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务编号，0表示无项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务编号，0表示无项目任务',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择质量状态',
      },
    },
    {
      fieldName: 'qty',
      label: '冻结数量，基准单位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结数量，基准单位',
      },
    },
    {
      fieldName: 'releasedQty',
      label: '已解冻数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入已解冻数量',
      },
    },
    {
      fieldName: 'reason',
      label: '冻结原因',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结原因',
      },
    },
    {
      fieldName: 'status',
      label: '状态：ACTIVE、PARTIAL、RELEASED、CANCELLED',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: [],
        placeholder: '请选择状态：ACTIVE、PARTIAL、RELEASED、CANCELLED',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '冻结幂等键',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结幂等键',
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
export function useGridColumns(): VxeTableGridOptions<WmsInvLockApi.InvLock>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'lockNo',
      title: '冻结编号',
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
      field: 'invOrgId',
      title: '库存组织编号',
      minWidth: 120,
    },
    {
      field: 'locId',
      title: '库位编号',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '物料编号',
      minWidth: 120,
    },
    {
      field: 'batchId',
      title: '批次编号',
      minWidth: 120,
    },
    {
      field: 'containerId',
      title: '容器编号，0表示散件',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目编号，0表示无项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务编号，0表示无项目任务',
      minWidth: 120,
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 120,
    },
    {
      field: 'qty',
      title: '冻结数量，基准单位',
      minWidth: 120,
    },
    {
      field: 'releasedQty',
      title: '已解冻数量',
      minWidth: 120,
    },
    {
      field: 'reason',
      title: '冻结原因',
      minWidth: 120,
    },
    {
      field: 'status',
      title: '状态：ACTIVE、PARTIAL、RELEASED、CANCELLED',
      minWidth: 120,
    },
    {
      field: 'idempotencyKey',
      title: '冻结幂等键',
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