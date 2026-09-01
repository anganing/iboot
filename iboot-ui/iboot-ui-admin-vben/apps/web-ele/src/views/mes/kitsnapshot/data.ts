import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesKitSnapshotApi } from '#/api/mes/kitsnapshot';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { MAT_RELATION_PROPS } from '#/views/mdm/relation-config';

import {
  OP_TASK_RELATION_PROPS,
  PROD_ORDER_RELATION_PROPS,
  WORK_ORDER_RELATION_PROPS,
} from '../relation-config';

/**
 * 列表的搜索表单。
 *
 * 齐套快照是齐套检查过账写入的账表，只查询与导出，所以没有 useFormSchema。
 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'checkNo',
      label: '检查单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入检查单号',
      },
    },
    {
      fieldName: 'prodOrderId',
      label: '生产工单',
      component: markRaw(RelationSelect),
      componentProps: PROD_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      component: markRaw(RelationSelect),
      componentProps: WORK_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'checkScope',
      label: '检查范围',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_KIT_CHECK_SCOPE, 'string'),
        placeholder: '请选择检查范围',
      },
    },
    {
      fieldName: 'result',
      label: '齐套结果',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_KIT_STATUS, 'string'),
        placeholder: '请选择齐套结果',
      },
    },
    {
      fieldName: 'checkTime',
      label: '检查时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段。账表不提供写操作，所以没有勾选列与操作列。 */
export function useGridColumns(): VxeTableGridOptions<MesKitSnapshotApi.KitSnapshot>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'checkNo', title: '检查单号', minWidth: 160 },
    {
      field: 'checkScope',
      title: '检查范围',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_KIT_CHECK_SCOPE },
      },
    },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'reqQty', title: '需求数量', minWidth: 110 },
    { field: 'availQty', title: '可用数量', minWidth: 110 },
    { field: 'reservedQty', title: '已预留数量', minWidth: 110 },
    { field: 'issuedQty', title: '已发料数量', minWidth: 110 },
    { field: 'shortQty', title: '缺料数量', minWidth: 110 },
    { field: 'altMatName', title: '替代料', minWidth: 150 },
    {
      field: 'result',
      title: '齐套结果',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_KIT_STATUS },
      },
    },
    {
      field: 'checkTime',
      title: '检查时间',
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
  ];
}
