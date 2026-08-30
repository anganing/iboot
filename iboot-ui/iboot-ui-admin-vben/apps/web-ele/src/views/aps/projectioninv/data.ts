import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsProjectionInvApi } from '#/api/aps/projectioninv';

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
      fieldName: 'scenarioId',
      label: '排程场景',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入排程场景',
      },
    },
    {
      fieldName: 'sourceBalanceId',
      label: '库存余额',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存余额',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存组织',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入工厂',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库',
      },
    },
    {
      fieldName: 'locId',
      label: '库位',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库位',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入物料编码',
      },
    },
    {
      fieldName: 'batchId',
      label: '批次',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入批次号',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入项目任务',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入库存范围',
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
      fieldName: 'qtyOnhand',
      label: '现存数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入现存数量',
      },
    },
    {
      fieldName: 'qtyReserved',
      label: '预留数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入预留数量',
      },
    },
    {
      fieldName: 'qtyFrozen',
      label: '冻结数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入冻结数量',
      },
    },
    {
      fieldName: 'qtyAvailable',
      label: '可用数量',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入可用数量',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
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
      fieldName: 'snapshotTime',
      label: '投影快照时间',
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
      label: '库存投影数据',
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
      fieldName: 'scenarioId',
      label: '排程场景',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入排程场景',
      },
    },
    {
      fieldName: 'sourceBalanceId',
      label: '库存余额',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存余额',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存组织',
      },
    },
    {
      fieldName: 'plantId',
      label: '工厂',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工厂',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入仓库',
      },
    },
    {
      fieldName: 'locId',
      label: '库位',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库位',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料',
      },
    },
    {
      fieldName: 'matCode',
      label: '物料编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入物料编码',
      },
    },
    {
      fieldName: 'batchId',
      label: '批次',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次',
      },
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号',
      },
    },
    {
      fieldName: 'projectId',
      label: '项目',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入项目任务',
      },
    },
    {
      fieldName: 'stockScope',
      label: '库存范围',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入库存范围',
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
      fieldName: 'qtyOnhand',
      label: '现存数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入现存数量',
      },
    },
    {
      fieldName: 'qtyReserved',
      label: '预留数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入预留数量',
      },
    },
    {
      fieldName: 'qtyFrozen',
      label: '冻结数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入冻结数量',
      },
    },
    {
      fieldName: 'qtyAvailable',
      label: '可用数量',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入可用数量',
      },
    },
    {
      fieldName: 'sourceUpdateTime',
      label: '来源数据更新时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '投影快照时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
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
export function useGridColumns(): VxeTableGridOptions<ApsProjectionInvApi.ProjectionInv>['columns'] {
  return [
  { type: 'checkbox', width: 40 },
  { type: 'seq', title: '序号', width: 60 },
    {
      field: 'sourceBalanceId',
      title: '库存余额',
      minWidth: 120,
    },
    {
      field: 'invOrgId',
      title: '库存组织',
      minWidth: 120,
    },
    {
      field: 'plantId',
      title: '工厂',
      minWidth: 120,
    },
    {
      field: 'whId',
      title: '仓库',
      minWidth: 120,
    },
    {
      field: 'locId',
      title: '库位',
      minWidth: 120,
    },
    {
      field: 'matId',
      title: '物料',
      minWidth: 120,
    },
    {
      field: 'matCode',
      title: '物料编码',
      minWidth: 120,
    },
    {
      field: 'batchId',
      title: '批次',
      minWidth: 120,
    },
    {
      field: 'lotNo',
      title: '批次号',
      minWidth: 120,
    },
    {
      field: 'projectId',
      title: '项目',
      minWidth: 120,
    },
    {
      field: 'projectTaskId',
      title: '项目任务',
      minWidth: 120,
    },
    {
      field: 'stockScope',
      title: '库存范围',
      minWidth: 120,
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 120,
    },
    {
      field: 'qtyOnhand',
      title: '现存数量',
      minWidth: 120,
    },
    {
      field: 'qtyReserved',
      title: '预留数量',
      minWidth: 120,
    },
    {
      field: 'qtyFrozen',
      title: '冻结数量',
      minWidth: 120,
    },
    {
      field: 'qtyAvailable',
      title: '可用数量',
      minWidth: 120,
    },
    {
      field: 'sourceUpdateTime',
      title: '来源数据更新时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'snapshotTime',
      title: '投影快照时间',
      minWidth: 120,
      formatter: 'formatDateTime',
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
      field: 'scenarioCode',
      title: '排程场景编码',
      minWidth: 120,
    },
    {
      field: 'scenarioName',
      title: '排程场景名称',
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