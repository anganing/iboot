import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

export function useReqGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'reqNo',
      label: '申请单号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入申请单号' },
    },
    {
      fieldName: 'prodOrderNo',
      label: '生产工单号',
      component: 'Input',
      componentProps: { clearable: true, placeholder: '请输入生产工单号' },
    },
    {
      fieldName: 'status',
      label: '状态',
      component: 'Select',
      componentProps: {
        clearable: true,
        options: getDictOptions(DICT_TYPE.MES_MATERIAL_REQ_STATUS, 'string'),
        placeholder: '待确认申请',
      },
      defaultValue: 'SUBMITTED',
    },
  ];
}

export function useReqGridColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReq>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'reqNo', title: '申请单号', minWidth: 150 },
    {
      field: 'reqType',
      title: '申请类型',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_TYPE },
      },
    },
    { field: 'prodOrderNo', title: '生产工单号', minWidth: 150 },
    { field: 'opTaskNo', title: '工序任务号', minWidth: 150 },
    { field: 'whName', title: '仓库', minWidth: 120 },
    {
      field: 'status',
      title: '状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_STATUS },
      },
    },
    // 作业入口从"整行点击"改为显式按钮，避免整行误触。
    // 本页只有「明细」1 个固定动作（确认出库在抽屉内），按规范 3.4 取 1 位档 100。
    { title: '操作', width: 100, fixed: 'right', slots: { default: 'actions' } },
  ];
}

export function useLineColumns(): VxeTableGridOptions<MesMaterialReqApi.MaterialReqLine>['columns'] {
  return [
    { type: 'seq', title: '序号', width: 60 },
    { field: 'lineNo', title: '行号', width: 70 },
    { field: 'matCode', title: '物料编码', minWidth: 130 },
    { field: 'matName', title: '物料名称', minWidth: 150 },
    { field: 'qty', title: '申请数量', width: 100 },
    { field: 'processedQty', title: '已处理', width: 90 },
    { field: 'lotNo', title: '指定批次', minWidth: 120 },
    {
      field: 'mustIssueBeforeStart',
      title: '开工前必发',
      width: 110,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.INFRA_BOOLEAN_STRING },
      },
    },
    { field: 'uomCode', title: '单位', width: 70 },
    {
      field: 'status',
      title: '明细状态',
      width: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MES_MATERIAL_REQ_LINE_STATUS },
      },
    },
  ];
}
