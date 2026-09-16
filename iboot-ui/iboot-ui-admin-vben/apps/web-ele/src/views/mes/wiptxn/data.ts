import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesWipTxnApi } from '#/api/mes/wiptxn';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';

import {
  OP_TASK_RELATION_PROPS,
  PROD_ORDER_RELATION_PROPS,
  WORK_ORDER_RELATION_PROPS,
} from '../relation-config';

/**
 * 列表的搜索表单。
 *
 * 在制流水是过账留痕的证据链，只查询与导出，所以没有 useFormSchema。
 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'txnNo',
      label: '事务编号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入事务编号',
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
      fieldName: 'txnType',
      label: '事务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_WIP_TXN_TYPE, 'string'),
        placeholder: '请选择事务类型',
      },
    },
    {
      fieldName: 'bizType',
      label: '来源业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_BIZ_TYPE, 'string'),
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
      fieldName: 'txnTime',
      label: '事务时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段。账表不提供写操作，所以没有勾选列与操作列。 */
export function useGridColumns(): VxeTableGridOptions<MesWipTxnApi.WipTxn>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    // txnNo 由编码规则 MES_WIP_TXN_NO 分配，唯一标识这条流水；工单号/工序任务号都是关联对象编码
    { field: 'txnNo', title: '事务编号', minWidth: 160, ...codeLinkColumn() },
    {
      field: 'txnType',
      title: '事务类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_WIP_TXN_TYPE },
      },
    },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'qty', title: '事务数量', minWidth: 110 },
    {
      field: 'fromStatus',
      title: '变更前状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_TASK_STATUS },
      },
    },
    {
      field: 'toStatus',
      title: '变更后状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_TASK_STATUS },
      },
    },
    {
      field: 'bizType',
      title: '来源业务类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_BIZ_TYPE } },
    },
    { field: 'bizNo', title: '来源业务单号', minWidth: 150 },
    { field: 'fromOpTaskNo', title: '来源工序任务', minWidth: 160 },
    { field: 'toOpTaskNo', title: '目标工序任务', minWidth: 160 },
    {
      field: 'txnTime',
      title: '事务时间',
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
