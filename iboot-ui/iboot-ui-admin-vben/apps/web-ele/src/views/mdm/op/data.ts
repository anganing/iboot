import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MdmOpApi } from '#/api/mdm/op';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
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
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        placeholder: '为空时按编码规则自动生成',
        maxLength: 64,
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'stdDuration',
      label: '标准时长（分钟）',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '固定工时场景取值',
        precision: 0,
      },
    },
    {
      fieldName: 'durationType',
      label: '时长类型',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions('mdm_duration_type', 'string'),
      },
      defaultValue: 'FIXED',
    },
    {
      fieldName: 'stdMinutesPerUnit',
      label: '单件加工工时（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '按件累加时必填',
        precision: 6,
      },
      dependencies: {
        triggerFields: ['durationType'],
        rules: (values) => {
          return values.durationType === 'PER_UNIT' ? 'required' : null;
        },
      },
    },
    {
      fieldName: 'setupMinutes',
      label: '准备工时（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
      },
    },
    {
      fieldName: 'teardownMinutes',
      label: '收尾工时（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
      },
    },
    {
      fieldName: 'queueMinutes',
      label: '排队等待（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
      },
    },
    {
      fieldName: 'moveMinutes',
      label: '转移时间（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        precision: 0,
      },
    },
    {
      fieldName: 'manQty',
      label: '标准人数',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'code',
      label: '编码',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入编码',
      },
    },
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'stdDuration',
      label: '标准时长（分钟）',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        placeholder: '请输入标准时长（分钟）',
        precision: 0,
      },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        placeholder: '请选择状态',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入备注',
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
export function useGridColumns(): VxeTableGridOptions<MdmOpApi.Op>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    {
      field: 'code',
      title: '编码',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名称',
      minWidth: 120,
    },
    {
      field: 'stdDuration',
      title: '标准时长（分钟）',
      minWidth: 120,
    },
    {
      field: 'durationType',
      title: '时长类型',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: 'mdm_duration_type' },
      },
    },
    {
      field: 'stdMinutesPerUnit',
      title: '单件工时（分钟）',
      minWidth: 120,
    },
    {
      field: 'setupMinutes',
      title: '准备工时',
      minWidth: 100,
    },
    {
      field: 'teardownMinutes',
      title: '收尾工时',
      minWidth: 100,
    },
    {
      field: 'queueMinutes',
      title: '排队等待',
      minWidth: 100,
    },
    {
      field: 'moveMinutes',
      title: '转移时间',
      minWidth: 100,
    },
    {
      field: 'manQty',
      title: '标准人数',
      minWidth: 90,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
