import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import {
  RES_RELATION_PROPS,
  SHIFT_RELATION_PROPS,
  TEAM_RELATION_PROPS,
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

/**
 * 工序任务列表行：物料与齐套状态在作业工单上，后端工序任务 RespVO 不带，
 * 页面查询后按 workOrderId 补全到行，故在此扩展而不动 api 类型。
 */
export interface OpTaskRow extends MesOpTaskApi.OpTask {
  kitStatus?: string;
  matCode?: string;
  matName?: string;
}

export function useTaskGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '工序任务号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入工序任务号' },
    },
    {
      fieldName: 'status',
      label: '任务状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_OP_TASK_STATUS, 'string'),
        placeholder: '待开工任务',
      },
      defaultValue: 'READY',
    },
  ];
}

export function useTaskGridColumns(): VxeTableGridOptions<MesOpTaskApi.OpTask>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 55 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 130 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 130 },
    // 物料编码/名称与齐套状态在作业工单上，index.vue 查询后按 workOrderId 补全到行
    { field: 'matCode', title: '物料编码', minWidth: 110 },
    { field: 'matName', title: '物料名称', minWidth: 120 },
    { field: 'opCode', title: '工序编码', minWidth: 100 },
    { field: 'opName', title: '工序名称', minWidth: 120 },
    { field: 'plannedQty', title: '计划数量', width: 90 },
    { field: 'resName', title: '已分配资源', minWidth: 110 },
    {
      field: 'plannedStartTime',
      title: '计划开始',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      field: 'plannedEndTime',
      title: '计划结束',
      minWidth: 150,
      formatter: 'formatDateTime',
    },
    {
      field: 'kitStatus',
      title: '齐套状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_KIT_STATUS },
      },
    },
    {
      field: 'status',
      title: '状态',
      width: 90,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_TASK_STATUS },
      },
    },
  ];
}

export function useDispatchFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'resId',
      label: '生产资源',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'operatorId',
      label: '作业人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'teamId',
      label: '作业班组',
      component: markRaw(RelationSelect),
      componentProps: TEAM_RELATION_PROPS,
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        maxLength: 500,
        placeholder: '请输入派工备注',
      },
    },
  ];
}
