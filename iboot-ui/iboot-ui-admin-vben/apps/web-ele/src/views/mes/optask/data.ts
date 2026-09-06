import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpTaskApi } from '#/api/mes/optask';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  OP_RELATION_PROPS,
  RES_RELATION_PROPS,
  USER_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { QTY_PROPS } from '../form-props';

/**
 * 工序动作表单：开工需要操作人员，暂停需要原因。
 *
 * 完工的对账数量只在两个都填时才由后端校验，所以都设为可选；
 * 跳序原因类型、原因、影响说明独立在跳序表单里，不混进普通动作。
 */
export function useOpTaskActionFormSchema(
  action: 'start' | 'pause',
): VbenFormSchema[] {
  return [
    {
      fieldName: 'operatorId',
      label: '操作人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
      dependencies: {
        triggerFields: [''],
        show: () => action === 'start',
      },
    },
    {
      fieldName: 'reason',
      label: '原因',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: action === 'pause' ? '请输入暂停原因' : '请输入开工备注',
        rows: 2,
      },
      dependencies: {
        triggerFields: [''],
        show: () => action === 'start' || action === 'pause',
      },
    },
  ];
}

/** 完工对账声明：留空时后端只校验累计数量，两个一起给才要求与累计值一致。 */
export function useOpTaskFinishFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'qualifiedQty',
      label: '累计合格数',
      component: 'InputNumber',
      componentProps: {
        ...QTY_PROPS,
        placeholder: '留空按后端累计值校验',
      },
    },
    {
      fieldName: 'scrapQty',
      label: '累计报废数',
      component: 'InputNumber',
      componentProps: {
        ...QTY_PROPS,
        placeholder: '留空按后端累计值校验',
      },
    },
    {
      fieldName: 'reason',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入完工备注',
        rows: 2,
      },
    },
  ];
}

/** 跳序申请表单：原因类型与原因必填，影响说明在待审批策略下后端也会要求。 */
export function useOpTaskSkipFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'skipReasonType',
      label: '原因类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_REASON_TYPE, 'string'),
        placeholder: '请选择跳序原因类型',
      },
    },
    {
      fieldName: 'reason',
      label: '跳序原因',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入跳序原因',
        rows: 2,
      },
    },
    {
      fieldName: 'effectRemark',
      label: '影响说明',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '对后续工序、质检与排程的影响',
        rows: 2,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskNo',
      label: '工序任务号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入工序任务号',
      },
    },
    {
      fieldName: 'workOrderNo',
      label: '作业工单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入作业工单号',
      },
    },
    {
      fieldName: 'opId',
      label: '标准工序',
      component: markRaw(RelationSelect),
      componentProps: OP_RELATION_PROPS,
    },
    {
      fieldName: 'resId',
      label: '分配资源',
      component: markRaw(RelationSelect),
      componentProps: RES_RELATION_PROPS,
    },
    {
      fieldName: 'keyFlag',
      label: '关键工序',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
        placeholder: '请选择是否关键工序',
      },
    },
    {
      fieldName: 'qcMode',
      label: '质检模式',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_QC_MODE, 'string'),
        placeholder: '请选择质检模式',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_OP_TASK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesOpTaskApi.OpTask>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '工序任务号', minWidth: 160 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 160 },
    { field: 'seq', title: '工序顺序', minWidth: 100 },
    { field: 'opCode', title: '工序编码', minWidth: 130 },
    { field: 'opName', title: '工序名称', minWidth: 150 },
    { field: 'parallelGroup', title: '并行组', minWidth: 110 },
    {
      field: 'execMode',
      title: '执行模式',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_EXEC_MODE },
      },
    },
    {
      field: 'keyFlag',
      title: '关键工序',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'plannedQty', title: '计划数量', minWidth: 110 },
    { field: 'completedQty', title: '已完成数量', minWidth: 110 },
    { field: 'scrapQty', title: '报废数量', minWidth: 110 },
    { field: 'resName', title: '分配资源', minWidth: 140 },
    {
      field: 'qcMode',
      title: '质检模式',
      minWidth: 120,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_QC_MODE } },
    },
    {
      field: 'skipPolicy',
      title: '跳序策略',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_SKIP_POLICY },
      },
    },
    {
      field: 'plannedStartTime',
      title: '计划开始时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_OP_TASK_STATUS },
      },
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
