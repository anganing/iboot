import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsInvReservationApi } from '#/api/wms/invreservation';

import { markRaw } from 'vue';

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
      fieldName: 'reservationNo',
      label: '预留编号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入预留编号',
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
      fieldName: 'bizLineNo',
      label: '来源行号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入来源业务行号',
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
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'qty',
      label: '预留数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入预留数量' },
    },
    {
      fieldName: 'releasedQty',
      label: '已处理数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '已释放或已核销数量' },
      defaultValue: 0,
    },
    {
      fieldName: 'status',
      label: '预留状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_reservation_status', 'string'),
        placeholder: '请选择预留状态',
      },
      defaultValue: 'ACTIVE',
    },
    {
      fieldName: 'expireTime',
      label: '失效时间',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择预留失效时间',
        showTime: true,
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '请输入预留幂等键',
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
      label: '库存预留数据',
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
      fieldName: 'reservationNo',
      label: '预留编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入预留编号',
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
      label: '预留状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_reservation_status', 'string'),
        placeholder: '请选择预留状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsInvReservationApi.InvReservation>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'reservationNo', title: '预留编号', minWidth: 160 },
    { field: 'bizNo', title: '来源单号', minWidth: 150 },
    { field: 'bizLineNo', title: '来源行号', minWidth: 100 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'whName', title: '仓库', minWidth: 130 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    { field: 'qty', title: '预留数量', minWidth: 110 },
    { field: 'releasedQty', title: '已处理数量', minWidth: 110 },
    {
      field: 'remainQty',
      title: '剩余数量',
      minWidth: 110,
      formatter: ({ row }) =>
        Number(row.qty ?? 0) - Number(row.releasedQty ?? 0),
    },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    {
      field: 'status',
      title: '预留状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: 'wms_reservation_status' },
      },
    },
    {
      field: 'expireTime',
      title: '失效时间',
      minWidth: 160,
      formatter: 'formatDateTime',
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
