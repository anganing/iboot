import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
  PROJECT_TASK_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import {
  BATCH_RELATION_PROPS,
  CONTAINER_RELATION_PROPS,
  INV_BALANCE_RELATION_PROPS,
  LOC_RELATION_PROPS,
  WH_RELATION_PROPS,
  ZONE_RELATION_PROPS,
} from '../relation-config';

/** 数量控件的公共配置 */
const QTY_PROPS = {
  class: '!w-full',
  controlsPosition: 'right' as const,
  precision: 6,
};

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
      fieldName: 'countNo',
      label: '盘点单号',
      rules: 'required',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'countType',
      label: '盘点类型',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_count_type', 'string'),
        placeholder: '请选择盘点类型',
      },
    },
    {
      fieldName: 'invOrgId',
      label: '库存组织',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_ORG_RELATION_PROPS,
    },
    {
      fieldName: 'whId',
      label: '仓库',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'zoneId',
      label: '库区',
      component: markRaw(RelationSelect),
      componentProps: {
        ...ZONE_RELATION_PROPS,
        placeholder: '留空表示全部库区',
      },
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '留空表示不限项目',
      },
    },
    {
      fieldName: 'snapshotTime',
      label: '快照时间',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        class: '!w-full',
        format: 'YYYY-MM-DD HH:mm:ss',
        placeholder: '请选择账面快照时间',
        showTime: true,
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'blindCountFlag',
      label: '是否盲盘',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_BOOLEAN_STRING, 'boolean'),
      },
      defaultValue: false,
    },
    {
      fieldName: 'status',
      label: '盘点状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_order_status', 'string'),
        placeholder: '请选择盘点状态',
      },
      defaultValue: 'DRAFT',
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

/** 导入的表单 */
export function useImportFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'file',
      label: '盘点单数据',
      component: 'Upload',
      rules: 'required',
      help: '仅允许导入 xls、xlsx 格式文件',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'countNo',
      label: '盘点单号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入盘点单号',
      },
    },
    {
      fieldName: 'countType',
      label: '盘点类型',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_count_type', 'string'),
        placeholder: '请选择盘点类型',
      },
    },
    {
      fieldName: 'whId',
      label: '仓库',
      component: markRaw(RelationSelect),
      componentProps: WH_RELATION_PROPS,
    },
    {
      fieldName: 'zoneId',
      label: '库区',
      component: markRaw(RelationSelect),
      componentProps: ZONE_RELATION_PROPS,
    },
    {
      fieldName: 'status',
      label: '盘点状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_order_status', 'string'),
        placeholder: '请选择盘点状态',
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
export function useGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrder>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'countNo', title: '盘点单号', minWidth: 160 },
    {
      field: 'countType',
      title: '盘点类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: 'wms_count_type' } },
    },
    { field: 'whName', title: '仓库', minWidth: 140 },
    { field: 'zoneName', title: '库区', minWidth: 140 },
    {
      field: 'snapshotTime',
      title: '快照时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      field: 'blindCountFlag',
      title: '是否盲盘',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    {
      field: 'status',
      title: '盘点状态',
      minWidth: 110,
      cellRender: { name: 'CellDict', props: { type: 'wms_order_status' } },
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

// ==================== 子表（盘点明细） ====================

/** 新增/修改的表单 */
export function useCountOrderLineFormSchema(): VbenFormSchema[] {
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
      fieldName: 'lineNo',
      label: '行号',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 1,
        precision: 0,
        placeholder: '请输入行号',
      },
    },
    {
      fieldName: 'balanceId',
      label: '库存余额',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: INV_BALANCE_RELATION_PROPS,
    },
    {
      fieldName: 'matId',
      label: '物料',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'batchId',
      label: '批次',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: BATCH_RELATION_PROPS,
    },
    {
      fieldName: 'locId',
      label: '库位',
      rules: 'required',
      component: markRaw(RelationSelect),
      componentProps: LOC_RELATION_PROPS,
    },
    {
      fieldName: 'containerId',
      label: '容器',
      component: markRaw(RelationSelect),
      componentProps: {
        ...CONTAINER_RELATION_PROPS,
        placeholder: '散件留空',
      },
    },
    {
      fieldName: 'qualityStatus',
      label: '质量状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_quality_status', 'string'),
        placeholder: '请选择质量状态',
      },
      defaultValue: 'QUALIFIED',
    },
    {
      fieldName: 'projectId',
      label: '归属项目',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_RELATION_PROPS,
        placeholder: '公共库存留空',
      },
    },
    {
      fieldName: 'projectTaskId',
      label: '项目任务',
      component: markRaw(RelationSelect),
      componentProps: {
        ...PROJECT_TASK_RELATION_PROPS,
        placeholder: '无项目任务留空',
      },
    },
    {
      fieldName: 'bookQty',
      label: '账面数量',
      rules: 'required',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, min: 0, placeholder: '请输入账面数量' },
    },
    {
      fieldName: 'actualQty',
      label: '实盘数量',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, min: 0, placeholder: '请输入实盘数量' },
    },
    {
      fieldName: 'diffQty',
      label: '差异数量',
      component: 'InputNumber',
      componentProps: { ...QTY_PROPS, placeholder: '实盘减账面，可为负数' },
    },
    {
      fieldName: 'adjustBizNo',
      label: '调整单号',
      component: 'Input',
      componentProps: {
        maxLength: 64,
        placeholder: '差异过账后的入出库单号',
      },
    },
    {
      fieldName: 'status',
      label: '明细状态',
      rules: 'required',
      component: 'Select',
      componentProps: {
        options: getDictOptions('wms_count_line_status', 'string'),
        placeholder: '请选择明细状态',
      },
      defaultValue: 'PENDING',
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
export function useCountOrderLineGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'matId',
      label: '物料',
      component: markRaw(RelationSelect),
      componentProps: MAT_RELATION_PROPS,
    },
    {
      fieldName: 'lotNo',
      label: '批次号',
      component: 'Input',
      componentProps: {
        clearable: true,
        placeholder: '请输入批次号',
      },
    },
    {
      fieldName: 'status',
      label: '明细状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions('wms_count_line_status', 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/** 列表的字段 */
export function useCountOrderLineGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', minWidth: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 140 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    { field: 'bookQty', title: '账面数量', minWidth: 110 },
    { field: 'actualQty', title: '实盘数量', minWidth: 110 },
    { field: 'diffQty', title: '差异数量', minWidth: 110 },
    {
      field: 'qualityStatus',
      title: '质量状态',
      minWidth: 100,
      cellRender: { name: 'CellDict', props: { type: 'wms_quality_status' } },
    },
    { field: 'adjustBizNo', title: '调整单号', minWidth: 150 },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: 'wms_count_line_status' },
      },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      title: '操作',
      width: 160,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
