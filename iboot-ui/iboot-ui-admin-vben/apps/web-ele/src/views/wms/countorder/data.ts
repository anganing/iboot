import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { markRaw } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { RelationSelect } from '#/components/relation-select';
import { codeLinkColumn, getRangePickerDefaultProps } from '#/utils';
import {
  INV_ORG_RELATION_PROPS,
  MAT_RELATION_PROPS,
  PROJECT_RELATION_PROPS,
} from '#/views/mdm/relation-config';

import { WH_RELATION_PROPS, ZONE_RELATION_PROPS } from '../relation-config';

/**
 * 新增/修改的表单。
 *
 * 没有明细区，这是盘点单与另三类单据最实质的差别：盘点行不是人录的，是领域 Service 的
 * `generateLines` 按"仓库 + 库区 + 物料集合"圈选 `wms_inv_balance` 生成的，`bookQty` 取当时的
 * 余额快照。表单只定义盘点范围，`matIds` 用来收窄抽盘范围，留空即全盘。
 *
 * 也没有"盘点状态"与"账面快照时间"两项：状态由领域 Service 写死为新建、快照时间由它按生成盘点行
 * 的时刻重取，两个字段填了也会被覆盖。摆出来只会让用户以为自己能定，实际是死字段。
 * 请求 VO 上它们仍是必填，由 `form.vue` 在提交时补值。
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
        options: getDictOptions(DICT_TYPE.WMS_COUNT_TYPE, 'string'),
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
      fieldName: 'matIds',
      label: '盘点物料',
      component: markRaw(RelationSelect),
      componentProps: {
        ...MAT_RELATION_PROPS,
        multiple: true,
        dialogTitle: '选择盘点物料',
        placeholder: '留空表示全盘（不限物料）',
      },
      // 抽盘、循环盘靠它收窄范围：不给这个字段就只能整仓全盘
      help: '盘点行由服务端按仓库、库区与这些物料圈选库存余额生成，留空表示不限物料',
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
      // 盲盘时录实盘界面不显示账面数量，防止盘点员照抄
      help: '盲盘时录入实盘数量的界面不显示账面数量与差异',
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
        options: getDictOptions(DICT_TYPE.WMS_COUNT_TYPE, 'string'),
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
        options: getDictOptions(DICT_TYPE.WMS_ORDER_STATUS, 'string'),
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
    { field: 'countNo', title: '盘点单号', minWidth: 160, ...codeLinkColumn() },
    {
      field: 'countType',
      title: '盘点类型',
      minWidth: 130,
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_COUNT_TYPE } },
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
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_ORDER_STATUS } },
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
    { field: 'remark', title: '备注', minWidth: 140 },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 160,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      // 固定四位：明细、冻结、录实盘、提交差异审批；编辑/删除/审批进度进「更多」
      width: 340,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

// ==================== 子表（盘点明细） ====================

/**
 * 录实盘表格的字段。
 *
 * 盘点明细没有新增/修改表单：行由服务端按余额快照生成，`bookQty` 是账实对比的基准，
 * 人工改它等于给账实一致留后门。界面上唯一能写的是 `actualQty` 与行备注，
 * 且只能经 `/submit-result` 一次性批量提交。
 *
 * @param blindCount 是否盲盘。盲盘时账面数量与差异数量不显示——盘点员看得见账面就会照抄，
 *   盘点结果失去意义（`03-WMS仓储库存流程.md` 流程四）。
 */
export function useCountResultEditorColumns(
  blindCount = false,
): VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', width: 80 },
    { field: 'matCode', title: '物料编码', minWidth: 140 },
    { field: 'matName', title: '物料名称', minWidth: 160 },
    { field: 'lotNo', title: '批次号', minWidth: 130 },
    { field: 'locCode', title: '库位', minWidth: 130 },
    {
      field: 'bookQty',
      title: '账面数量',
      width: 110,
      visible: !blindCount,
    },
    {
      field: 'diffQty',
      title: '差异数量',
      width: 110,
      visible: !blindCount,
    },
    {
      field: 'actualQty',
      title: '实盘数量',
      width: 170,
      slots: { default: 'actualQty' },
    },
    {
      field: 'remark',
      title: '备注',
      minWidth: 180,
      slots: { default: 'lineRemark' },
    },
    {
      field: 'status',
      title: '明细状态',
      width: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_COUNT_LINE_STATUS },
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
        options: getDictOptions(DICT_TYPE.WMS_COUNT_LINE_STATUS, 'string'),
        placeholder: '请选择明细状态',
      },
    },
  ];
}

/**
 * 明细列表的字段（只读）。
 *
 * 没有勾选列与操作列：明细的增删改端点已撤销，这个列表只用来看盘点行与账实对比结果，
 * 录实盘走列表页的"录实盘"按钮批量提交。
 */
export function useCountOrderLineGridColumns(): VxeTableGridOptions<WmsCountOrderApi.CountOrderLine>['columns'] {
  return [
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
      cellRender: { name: 'CellDict', props: { type: DICT_TYPE.WMS_QUALITY_STATUS } },
    },
    { field: 'adjustBizNo', title: '调整单号', minWidth: 150 },
    {
      field: 'status',
      title: '明细状态',
      minWidth: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.WMS_COUNT_LINE_STATUS },
      },
    },
    { field: 'remark', title: '备注', minWidth: 140 },
  ];
}
