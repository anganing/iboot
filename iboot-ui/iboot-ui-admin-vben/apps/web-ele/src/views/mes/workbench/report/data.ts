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
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../../form-props';

export function useTaskGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '工序任务号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入工序任务号' },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入生产工单号' },
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '任务状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_OP_TASK_STATUS, 'string'),
        placeholder: '待执行任务',
      },
    },
  ];
}

export function useTaskGridColumns(): VxeTableGridOptions<MesOpTaskApi.OpTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '工序任务号', minWidth: 150 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 150 },
    { field: 'opCode', title: '工序编码', minWidth: 120 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'plannedQty', title: '计划数量', width: 100 },
    { field: 'completedQty', title: '已完成', width: 100 },
    { field: 'scrapQty', title: '已报废', width: 100 },
    { field: 'resName', title: '生产资源', minWidth: 130 },
    {
      field: 'status',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_TASK_STATUS },
      },
    },
    // 作业入口从"整行点击"改为显式按钮，避免整行误触；7 个作业页统一叫「明细」。
    // 本页只有 1 个固定动作（开工/报工/完工都在抽屉内），按规范 3.4 取 1 位档 100。
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}

export function useReportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'workerId',
      label: '报工人员',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'qtyGood',
      label: '合格数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: QTY_PROPS,
      defaultValue: 0,
    },
    {
      fieldName: 'qtyScrap',
      label: '报废数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: QTY_PROPS,
      defaultValue: 0,
    },
    {
      fieldName: 'qtyRework',
      label: '返工数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: QTY_PROPS,
      defaultValue: 0,
    },
    {
      fieldName: 'resId',
      label: '生产资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'shiftId',
      label: '班次',
      component: markRaw(RelationSelect),
      componentProps: SHIFT_RELATION_PROPS,
    },
    {
      fieldName: 'startTime',
      label: '作业开始',
      rules: 'required',
      component: 'DatePicker',
      componentProps: DATETIME_PROPS,
    },
    {
      fieldName: 'endTime',
      label: '作业结束',
      rules: 'required',
      component: 'DatePicker',
      componentProps: DATETIME_PROPS,
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        rows: 2,
        maxLength: 500,
        placeholder: '请输入报工备注',
      },
    },
  ];
}
