import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskLockApi } from '#/api/aps/tasklock';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import { USER_RELATION_PROPS } from '#/views/mdm/relation-config';

import { APS_TASK_RELATION_PROPS } from '../relation-config';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'taskId',
      label: '排程任务',
      component: markRaw(RelationSelect),
      componentProps: APS_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'lockType',
      label: '锁定类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_TASK_LOCK_TYPE, 'string'),
        placeholder: '请选择锁定类型',
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_TASK_LOCK_STATUS, 'string'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'lockedBy',
      label: '锁定人',
      component: markRaw(RelationSelect),
      componentProps: USER_RELATION_PROPS,
    },
    {
      fieldName: 'lockedTime',
      label: '锁定时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<ApsTaskLockApi.TaskLock>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'taskNo', title: '任务编号', minWidth: 160 },
    { field: 'opCode', title: '工序编码', minWidth: 110 },
    { field: 'opName', title: '工序名称', minWidth: 130 },
    { field: 'resCode', title: '资源编码', minWidth: 110 },
    {
      field: 'lockType',
      title: '锁定类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_TASK_LOCK_TYPE },
      },
    },
    { field: 'reason', title: '锁定原因', minWidth: 200 },
    { field: 'lockedByName', title: '锁定人', minWidth: 120 },
    {
      field: 'lockedTime',
      title: '锁定时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'unlockTime',
      title: '解锁时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_TASK_LOCK_STATUS },
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
