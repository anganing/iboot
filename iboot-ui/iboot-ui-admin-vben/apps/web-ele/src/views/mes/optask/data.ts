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
} from '#/views/mdm/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../form-props';
import {
  ROUTE_OP_RELATION_PROPS,
  ROUTE_RELATION_PROPS,
  WORK_ORDER_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 工序编码、名称、顺序、并行组、关键标记、执行模式都是路线工序的快照，由后端带出；
 * 工艺路线只是为了限定路线工序的选择范围，不提交给后端。
 * 状态不在表单里：工序任务状态只能由开工、暂停、完工、跳序等动作推进，新建时后端兜底为 PENDING。
 */
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
      fieldName: 'taskNo',
      label: '工序任务号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'workOrderId',
      label: '作业工单',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WORK_ORDER_RELATION_PROPS,
    },
    {
      fieldName: 'routeId',
      label: '工艺路线',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: {
        ...ROUTE_RELATION_PROPS,
        placeholder: '先选路线再挑工序',
      },
    },
    {
      fieldName: 'routeOpId',
      label: '路线工序',
      rules: 'required',
      component: markRaw(RelationSelect),
      // 工序必须限定在所选路线下，否则会挑到别条路线的工序
      dependencies: {
        triggerFields: ['routeId'],
        componentProps: (values) => ({
          ...ROUTE_OP_RELATION_PROPS,
          disabled: !values.routeId,
          extraParams: { routeId: values.routeId },
          placeholder: values.routeId ? '请选择路线工序' : '请先选择工艺路线',
        }),
      },
    },
    {
      fieldName: 'plannedQty',
      label: '计划数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '请输入计划数量' },
    },
    {
      fieldName: 'qcMode',
      label: '质检模式',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_QC_MODE, 'string'),
        placeholder: '请选择质检模式',
      },
      defaultValue: 'NONE',
    },
    {
      fieldName: 'skipPolicy',
      label: '跳序策略',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_POLICY, 'string'),
        placeholder: '请选择跳序策略',
      },
      defaultValue: 'NOT_ALLOWED',
    },
    {
      fieldName: 'resId',
      label: '分配资源',
      component: markRaw(RelationSelect),
      componentProps: {
        ...RES_RELATION_PROPS,
        placeholder: '未分配可留空',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'plannedStartTime',
      label: '计划开始时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划开始时间' },
    },
    {
      fieldName: 'plannedEndTime',
      label: '计划结束时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '请选择计划结束时间' },
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
    { type: 'checkbox', width: 40 },
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
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
