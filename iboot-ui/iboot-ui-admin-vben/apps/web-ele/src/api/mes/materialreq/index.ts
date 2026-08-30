import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesMaterialReqApi {
  /** MES-领退补料申请明细信息 */
  export interface MaterialReqLine {
            id: number; // 主键
            reqId?: number; // 领退补料申请编号
            lineNo?: number; // 行号
            routeOpMatId: number; // 路线工序用料编号
            matId?: number; // 物料编号
            matCode?: string; // 物料编码快照
            matName?: string; // 物料名称快照
            matSpec: string; // 物料规格快照
            qty?: number; // 申请数量
            processedQty?: number; // WMS已处理数量
            uomId?: number; // 计量单位编号
            uomCode?: string; // 计量单位编码快照
            batchId: number; // 指定退料批次编号
            lotNo: string; // 批次号快照
            mustIssueBeforeStart?: boolean; // 是否必须在开工前发料
            status?: string; // 明细状态
            remark: string; // 备注
  }

  /** MES-领退补料申请信息 */
  export interface MaterialReq {
    id: number; // 主键
    reqNo?: string; // 领退补料申请单号
    prodOrderId?: number; // 生产工单编号
    prodOrderNo?: string; // 生产工单号快照
    workOrderId?: number; // 作业工单编号，0表示工单级申请
    opTaskId?: number; // 工序任务编号，0表示非工序级申请
    reqType?: string; // 申请类型：ISSUE、RETURN、SUPPLEMENT
    invOrgId?: number; // 库存组织编号
    whId?: number; // 目标仓库或来源仓库编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    wmsBizNo: string; // WMS生成的出入库单号
    status?: string; // 状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED
    idempotencyKey: string; // 创建幂等键
    remark: string; // 备注
  }
}

/** 查询MES-领退补料申请分页 */
export function getMaterialReqPage(params: PageParam) {
  return requestClient.get<PageResult<MesMaterialReqApi.MaterialReq>>(
    '/mes/material-req/page',
    { params },
  );
}

/** 查询MES-领退补料申请详情 */
export function getMaterialReq(id: number) {
  return requestClient.get<MesMaterialReqApi.MaterialReq>(
    `/mes/material-req/get?id=${id}`,
  );
}

/** 新增MES-领退补料申请 */
export function createMaterialReq(data: MesMaterialReqApi.MaterialReq) {
  return requestClient.post('/mes/material-req/create', data);
}

/** 修改MES-领退补料申请 */
export function updateMaterialReq(data: MesMaterialReqApi.MaterialReq) {
  return requestClient.put('/mes/material-req/update', data);
}

/** 删除MES-领退补料申请 */
export function deleteMaterialReq(id: number) {
  return requestClient.delete(`/mes/material-req/delete?id=${id}`);
}

/** 批量删除MES-领退补料申请 */
export function deleteMaterialReqList(ids: number[]) {
  return requestClient.delete(
    `/mes/material-req/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-领退补料申请 */
export function exportMaterialReq(params: any) {
  return requestClient.download('/mes/material-req/export-excel', { params });
}

/** 下载MES-领退补料申请导入模板 */
export function importMaterialReqTemplate() {
  return requestClient.download('/mes/material-req/get-import-template');
}

/** 导入MES-领退补料申请 */
export function importMaterialReq(file: File) {
  return requestClient.upload('/mes/material-req/import', { file });
}


// ==================== 子表（MES-领退补料申请明细） ====================

/** 获得MES-领退补料申请明细分页 */
export function getMaterialReqLinePage(params: PageParam) {
  return requestClient.get<PageResult<MesMaterialReqApi.MaterialReqLine>>(
    `/mes/material-req/material-req-line/page`,
    { params },
  );
}
/** 新增MES-领退补料申请明细 */
export function createMaterialReqLine(data: MesMaterialReqApi.MaterialReqLine) {
  return requestClient.post(`/mes/material-req/material-req-line/create`, data);
}

/** 修改MES-领退补料申请明细 */
export function updateMaterialReqLine(data: MesMaterialReqApi.MaterialReqLine) {
  return requestClient.put(`/mes/material-req/material-req-line/update`, data);
}

/** 删除MES-领退补料申请明细 */
export function deleteMaterialReqLine(id: number) {
  return requestClient.delete(`/mes/material-req/material-req-line/delete?id=${id}`);
}

/** 批量删除MES-领退补料申请明细 */
export function deleteMaterialReqLineList(ids: number[]) {
  return requestClient.delete(
    `/mes/material-req/material-req-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得MES-领退补料申请明细 */
export function getMaterialReqLine(id: number) {
  return requestClient.get<MesMaterialReqApi.MaterialReqLine>(
    `/mes/material-req/material-req-line/get?id=${id}`,
  );
}
