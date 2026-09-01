import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { ApsTaskDepApi } from '#/api/aps/taskdep';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';

import {
  APS_TASK_RELATION_PROPS,
  APS_VERSION_RELATION_PROPS,
} from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * <p>版本编号不出现在表单里：依赖必须落在前置任务所属版本上，由后端从任务反推。
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
      fieldName: 'fromTaskId',
      label: '前置任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: APS_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'toTaskId',
      label: '后置任务',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: APS_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'depType',
      label: '依赖类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.APS_TASK_DEP_TYPE, 'string'),
        placeholder: '请选择依赖类型',
      },
      defaultValue: 'FINISH_START',
    },
    {
      fieldName: 'lagMinutes',
      label: '间隔分钟数',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
        placeholder: '例如冷却等待 30 分钟',
      },
      defaultValue: 0,
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
      formItemClass: 'col-span-2',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'versionId',
      label: '排程版本',
      component: markRaw(RelationSelect),
      componentProps: APS_VERSION_RELATION_PROPS,
    },
    {
      fieldName: 'fromTaskId',
      label: '前置任务',
      component: markRaw(RelationSelect),
      componentProps: APS_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'toTaskId',
      label: '后置任务',
      component: markRaw(RelationSelect),
      componentProps: APS_TASK_RELATION_PROPS,
    },
    {
      fieldName: 'depType',
      label: '依赖类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.APS_TASK_DEP_TYPE, 'string'),
        placeholder: '请选择依赖类型',
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
export function useGridColumns(): VxeTableGridOptions<ApsTaskDepApi.TaskDep>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'versionNo', title: '版本号', minWidth: 90 },
    { field: 'fromTaskNo', title: '前置任务编号', minWidth: 160 },
    { field: 'fromOpCode', title: '前置工序编码', minWidth: 130 },
    { field: 'fromOpName', title: '前置工序', minWidth: 130 },
    { field: 'toTaskNo', title: '后置任务编号', minWidth: 160 },
    { field: 'toOpCode', title: '后置工序编码', minWidth: 130 },
    { field: 'toOpName', title: '后置工序', minWidth: 130 },
    {
      field: 'depType',
      title: '依赖类型',
      minWidth: 130,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.APS_TASK_DEP_TYPE },
      },
    },
    { field: 'lagMinutes', title: '间隔分钟', minWidth: 100 },
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
