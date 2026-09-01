import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesDispositionApi } from '#/api/mes/disposition';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import { DATETIME_PROPS, QTY_PROPS } from '../form-props';
import { QC_TASK_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 处置数量不能超过质检任务的不合格数量，审批通过或驳回时审批人必填，规则都在后端 Service 校验。
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
      fieldName: 'dispositionNo',
      label: '处置单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '留空由编码规则自动分配',
      },
    },
    {
      fieldName: 'taskId',
      label: '质检任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: QC_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'dispositionType',
      label: '处置类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_DISPOSITION_TYPE, 'string'),
        placeholder: '请选择处置类型',
      },
      defaultValue: 'REWORK',
    },
    {
      fieldName: 'qty',
      label: '处置数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '不能大于任务不合格数量' },
    },
    {
      fieldName: 'targetBizType',
      label: '联动业务类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_BIZ_TYPE, 'string'),
        placeholder: '联动返工/报废单据时填写',
      },
    },
    {
      fieldName: 'targetBizNo',
      label: '联动业务单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '联动单据生成后回填',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MES_DISPOSITION_STATUS, 'string'),
        placeholder: '请选择状态',
      },
      defaultValue: 'DRAFT',
    },
    {
      fieldName: 'approvedBy',
      label: '审批人',
      component: markRaw(RelationSelect),
      componentProps: {
        ...USER_RELATION_PROPS,
        placeholder: '已批准或已驳回时必填',
      },
      defaultValue: 0,
    },
    {
      fieldName: 'approveTime',
      label: '审批时间',
      component: 'DatePicker',
      componentProps: { ...DATETIME_PROPS, placeholder: '留空取当前时间' },
    },
    {
      fieldName: 'idempotencyKey',
      label: '幂等键',
      component: 'Input',
      componentProps: {
        maxLength: 128,
        placeholder: '业务自动触发时用于去重，手工建单可留空',
      },
    },
    {
      fieldName: 'remark',
      label: '处置说明',
      component: 'Textarea',
      componentProps: {
        maxLength: 500,
        placeholder: '请输入处置说明',
        rows: 2,
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'dispositionNo',
      label: '处置单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入处置单号',
      },
    },
    {
      fieldName: 'taskId',
      label: '质检任务',
      component: markRaw(RelationSelect),
      componentProps: QC_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'dispositionType',
      label: '处置类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DISPOSITION_TYPE, 'string'),
        placeholder: '请选择处置类型',
      },
    },
    {
      fieldName: 'targetBizNo',
      label: '联动业务单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入联动业务单号',
      },
    },
    {
      fieldName: 'approvedBy',
      label: '审批人',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_DISPOSITION_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'approveTime',
      label: '审批时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<MesDispositionApi.Disposition>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'dispositionNo', title: '处置单号', minWidth: 160 },
    { field: 'taskNo', title: '质检任务号', minWidth: 160 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    {
      field: 'dispositionType',
      title: '处置类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DISPOSITION_TYPE },
      },
    },
    { field: 'qty', title: '处置数量', minWidth: 110 },
    {
      field: 'targetBizType',
      title: '联动业务类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.MES_BIZ_TYPE } },
    },
    { field: 'targetBizNo', title: '联动业务单号', minWidth: 150 },
    { field: 'approvedByName', title: '审批人', minWidth: 120 },
    {
      field: 'approveTime',
      title: '审批时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_DISPOSITION_STATUS },
      },
    },
    { field: 'remark', title: '处置说明', minWidth: 160 },
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
