import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvLockApi } from '#/api/wms/invlock';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import {
  BATCH_RELATION_PROPS,
  CONTAINER_RELATION_PROPS,
  LOC_RELATION_PROPS,
  WH_RELATION_PROPS,
} from '../relation-config';

/** 数量控件的公共配置 */
const QTY_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  min: 0,
  precision: 6,
};

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
        maxLength: 64,
        placeholder: '请输入冻结编号',
      },
    },
    {
      fieldName: 'bizNo',
      label: '来源单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'batchId',
      label: '批次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: BATCH_RELATION_PROPS,
    },
    {
      fieldName: 'containerId',
      label: '容器',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CONTAINER_RELATION_PROPS,
        placeholder: '散件留空',
      },
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_TASK_RELATION_PROPS,
        placeholder: '无项目任务留空',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_QUALITY_STATUS, 'string'),
        placeholder: '请选择质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'qty',
      label: '冻结数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入冻结数量' },
    },
    {
      fieldName: 'releasedQty',
      label: '已解冻数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入已解冻数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'reason',
      label: '冻结原因',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入冻结原因',
        rows: 2,
      },
    },
    {
      fieldName: 'status',
      label: '冻结状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.WMS_LOCK_STATUS, 'string'),
        placeholder: '请选择冻结状态',
      },
      defaultValue: 'ACTIVE',
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入冻结幂等键',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入备注',
        rows: 2,
      },
    },
  ];
}

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '库存冻结数据',
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
      fieldName: 'bizNo',
      label: '来源单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入来源业务单号',
      },
    },
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '冻结状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.WMS_LOCK_STATUS, 'string'),
        placeholder: '请选择冻结状态',
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
    { field: 'lockNo', title: '冻结编号', minWidth: 160 },
    { field: 'bizNo', title: '来源单号', minWidth: 150 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 130 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    { field: 'qty', title: '冻结数量', minWidth: 110 },
    { field: 'releasedQty', title: '已解冻数量', minWidth: 110 },
    {
      field: 'remainQty',
      title: '剩余冻结',
      minWidth: 110,
      formatter: ({ row }) =>
        Number(row.qty ?? 0) - Number(row.releasedQty ?? 0),
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    { field: 'reason', title: '冻结原因', minWidth: 180 },
    {
      field: 'status',
      title: '冻结状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_LOCK_STATUS } },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
