import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesOpSkipRecordApi } from '#/api/mes/opskiprecord';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import { DATETIME_PROPS } from '../form-props';
import { OP_TASK_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 能否跳序、是否需要审批由工序任务的跳序策略决定，后端按策略裁决；
 * 已生效的记录只允许补充影响说明与备注。
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
      fieldName: 'opTaskId',
      label: '被跳过工序任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'skipReasonType',
      label: '跳过原因类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_REASON_TYPE, 'string'),
        placeholder: '请选择跳过原因类型',
      },
      defaultValue: 'NOT_APPLICABLE',
    },
    {
      fieldName: 'skipReason',
      label: '跳过原因',
      rules: 'required',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入跳过原因',
        rows: 2,
      },
    },
    {
      fieldName: 'operatorId',
      label: '操作人员',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'approveStatus',
      label: '审批状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_APPROVE_STATUS, 'string'),
        placeholder: '请选择审批状态',
      },
      defaultValue: 'NOT_REQUIRED',
    },
    {
      fieldName: 'approvedBy',
      label: '审批人员',
      component: markRaw(RelationSelect),
      componentProps: {
        ...USER_RELATION_PROPS,
        placeholder: '已批准或已驳回时必填',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'skipTime',
      label: '实际跳过时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '留空取当前时间' },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_SKIP_RECORD_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'PENDING',
    },
    {
      fieldName: 'effectRemark',
      label: '影响说明',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '对后续工序、质检及排程的影响',
        rows: 2,
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

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'opTaskId',
      label: '工序任务',
      component: markRaw(RelationSelect),
      componentProps: OP_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'skipReasonType',
      label: '跳过原因类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_SKIP_REASON_TYPE, 'string'),
        placeholder: '请选择跳过原因类型',
      },
    },
    {
      fieldName: 'operatorId',
      label: '操作人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'approvedBy',
      label: '审批人员',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'approveStatus',
      label: '审批状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_SKIP_APPROVE_STATUS, 'string'),
        placeholder: '请选择审批状态',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_SKIP_RECORD_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'skipTime',
      label: '跳过时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesOpSkipRecordApi.OpSkipRecord>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 160 },
    { field: 'opName', title: '工序名称', minWidth: 140 },
    { field: 'workOrderNo', title: '作业工单号', minWidth: 160 },
    {
      field: 'skipReasonType',
      title: '跳过原因类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_SKIP_REASON_TYPE },
      },
    },
    { field: 'skipReason', title: '跳过原因', minWidth: 180 },
    { field: 'operatorName', title: '操作人员', minWidth: 120 },
    { field: 'approvedByName', title: '审批人员', minWidth: 120 },
    {
      field: 'approveStatus',
      title: '审批状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_SKIP_APPROVE_STATUS },
      },
    },
    {
      field: 'approvalStatus',
      title: '流程审批',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS },
      },
    },
    { field: 'approvalReason', title: '审批意见', minWidth: 160 },
    {
      field: 'skipTime',
      title: '跳过时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_SKIP_RECORD_STATUS },
      },
    },
    { field: 'effectRemark', title: '影响说明', minWidth: 180 },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      // 固定“详情 + 撤销”2 位，低频的审批进度进文字“更多”，共 3 位。
      // 本页不挂编码链接列：没有能唯一标识本行的业务编码（opTaskNo 是被跳过工序的编号，
      // 同一工序撤销后可再次跳序，不唯一），详情只走操作列按钮，见 待决问题.md。
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
