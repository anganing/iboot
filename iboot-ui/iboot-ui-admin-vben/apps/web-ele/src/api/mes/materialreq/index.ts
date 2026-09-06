import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesMaterialReqApi {
  /**
   * MES-领退补料申请。
   *
   * 生产工单号快照与项目维度由后端从生产工单继承；WMS 单号由 WMS 回写，前端只读。
   */
  export interface MaterialReq {
    id?: number;
    reqNo?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    reqType?: string;
    invOrgId?: number;
    whId?: number;
    whName?: string;
    projectId?: number;
    projectName?: string;
    projectTaskId?: number;
    wmsBizNo?: string;
    status?: string;
    idempotencyKey?: string;
    remark?: string;
    createTime?: string;
  }

  export interface MaterialReqCreateReq {
    prodOrderId: number;
    workOrderId?: number;
    opTaskId?: number;
    reqType: string;
    invOrgId: number;
    whId: number;
    lines?: Array<{
      matId: number;
      qty: number;
      uomId?: number;
      batchId?: number;
      routeOpMatId?: number;
      mustIssueBeforeStart?: boolean;
      remark?: string;
    }>;
    remark?: string;
    idempotencyKey?: string;
  }

  export interface MaterialReqActionReq {
    id: number;
    idempotencyKey?: string;
    reason?: string;
  }

  export interface MaterialReqConfirmReq {
    id: number;
    idempotencyKey?: string;
    remark?: string;
    lines?: Array<{
      lineNo: number;
      confirmQty?: number;
      locId?: number;
    }>;
  }

  /**
   * MES-领退补料申请明细。
   *
   * 物料、单位、批次号快照由后端补全；WMS 已处理数量由 WMS 回写，前端只读。
   */
  export interface MaterialReqLine {
    id?: number;
    reqId?: number;
    lineNo?: number;
    routeOpMatId?: number;
    matId?: number;
    matCode?: string;
    matName?: string;
    matSpec?: string;
    qty?: number;
    processedQty?: number;
    uomId?: number;
    uomCode?: string;
    batchId?: number;
    lotNo?: string;
    mustIssueBeforeStart?: boolean;
    status?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询领退补料申请分页 */
export function getMaterialReqPage(params: PageParam) {
  return requestClient.get<PageResult<MesMaterialReqApi.MaterialReq>>(
    '/mes/material-req/page',
    { params },
  );
}

/** 查询领退补料申请详情 */
export function getMaterialReq(id: number) {
  return requestClient.get<MesMaterialReqApi.MaterialReq>(
    `/mes/material-req/get?id=${id}`,
  );
}

/** 创建领退补料申请 */
export function createMaterialReq(
  data: MesMaterialReqApi.MaterialReqCreateReq,
) {
  return requestClient.post('/mes/material-req/create', data);
}

/** 修改领退补料申请 */
export function updateMaterialReq(data: MesMaterialReqApi.MaterialReq) {
  return requestClient.put('/mes/material-req/update', data);
}

/** 删除领退补料申请 */
export function deleteMaterialReq(id: number) {
  return requestClient.delete(`/mes/material-req/delete?id=${id}`);
}

/** 批量删除领退补料申请 */
export function deleteMaterialReqList(ids: number[]) {
  return requestClient.delete(
    `/mes/material-req/delete-list?ids=${ids.join(',')}`,
  );
}

/** 提交领退补料申请 */
export function submitMaterialReq(
  data: MesMaterialReqApi.MaterialReqActionReq,
) {
  return requestClient.post('/mes/material-req/submit', data);
}

/** 确认领退补料申请 */
export function confirmMaterialReq(
  data: MesMaterialReqApi.MaterialReqConfirmReq,
) {
  return requestClient.post('/mes/material-req/confirm', data);
}

/** 取消领退补料申请 */
export function cancelMaterialReq(
  data: MesMaterialReqApi.MaterialReqActionReq,
) {
  return requestClient.post('/mes/material-req/cancel', data);
}

/** 导出领退补料申请 */
export function exportMaterialReq(params: any) {
  return requestClient.download('/mes/material-req/export-excel', { params });
}

// ==================== 子表（领退补料申请明细） ====================

/** 查询申请明细分页 */
export function getMaterialReqLinePage(params: PageParam & { reqId: number }) {
  return requestClient.get<PageResult<MesMaterialReqApi.MaterialReqLine>>(
    '/mes/material-req/material-req-line/page',
    { params },
  );
}

/** 查询申请明细详情 */
export function getMaterialReqLine(id: number) {
  return requestClient.get<MesMaterialReqApi.MaterialReqLine>(
    `/mes/material-req/material-req-line/get?id=${id}`,
  );
}

/** 新增申请明细 */
export function createMaterialReqLine(
  data: MesMaterialReqApi.MaterialReqLine,
) {
  return requestClient.post('/mes/material-req/material-req-line/create', data);
}

/** 修改申请明细 */
export function updateMaterialReqLine(
  data: MesMaterialReqApi.MaterialReqLine,
) {
  return requestClient.put('/mes/material-req/material-req-line/update', data);
}

/** 删除申请明细 */
export function deleteMaterialReqLine(id: number) {
  return requestClient.delete(
    `/mes/material-req/material-req-line/delete?id=${id}`,
  );
}

/** 批量删除申请明细 */
export function deleteMaterialReqLineList(ids: number[]) {
  return requestClient.delete(
    `/mes/material-req/material-req-line/delete-list?ids=${ids.join(',')}`,
  );
}
