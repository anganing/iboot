import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsTransferOrderApi {
  /** WMS-库存转移调拨明细信息 */
  export interface TransferOrderLine {
            id: number; // 主键
            orderId?: number; // 转移调拨单编号
            lineNo?: number; // 行号
            matId?: number; // 物料编号
            matCode?: string; // 物料编码快照
            matName?: string; // 物料名称快照
            batchId?: number; // 批次编号
            lotNo?: string; // 批次号快照
            containerId?: number; // 容器编号，0表示散件
            fromLocId?: number; // 来源库位编号
            toLocId?: number; // 目标库位编号
            fromStockScope?: string; // 来源库存范围
            toStockScope?: string; // 目标库存范围
            fromProjectId?: number; // 来源项目编号，0表示公共库存
            toProjectId?: number; // 目标项目编号，0表示公共库存
            fromProjectTaskId?: number; // 来源项目任务编号，0表示无项目任务
            toProjectTaskId?: number; // 目标项目任务编号，0表示无项目任务
            qualityStatusFrom?: string; // 来源质量状态
            qualityStatusTo?: string; // 目标质量状态
            uomId?: number; // 基准计量单位编号
            uomCode?: string; // 基准单位编码快照
            planQty?: number; // 计划转移数量
            movedQty?: number; // 已搬运数量
            postedQty?: number; // 已过账数量
            status?: string; // 明细状态
            remark: string; // 备注
  }

  /** WMS-库存转移调拨单信息 */
  export interface TransferOrder {
    id: number; // 主键
    transferNo?: string; // 转移或调拨单号
    transferType?: string; // 转移类型：LOC_TRANSFER、WH_TRANSFER、INV_ORG_TRANSFER、PROJECT_TRANSFER、QUALITY_TRANSFER
    sourceType: string; // 来源业务类型
    sourceBizNo: string; // 来源业务单号
    fromInvOrgId?: number; // 来源库存组织编号
    toInvOrgId?: number; // 目标库存组织编号
    fromWhId?: number; // 来源仓库编号
    toWhId?: number; // 目标仓库编号
    fromProjectId?: number; // 来源项目编号，0表示公共库存
    toProjectId?: number; // 目标项目编号，0表示公共库存
    hookMode?: string; // 外部设备触发方式：NONE、WCS、AGV
    status?: string; // 状态：DRAFT、SUBMITTED、APPROVED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION
    idempotencyKey: string; // 创建幂等键
    remark: string; // 备注
  }
}

/** 查询WMS-库存转移调拨单分页 */
export function getTransferOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsTransferOrderApi.TransferOrder>>(
    '/wms/transfer-order/page',
    { params },
  );
}

/** 查询WMS-库存转移调拨单详情 */
export function getTransferOrder(id: number) {
  return requestClient.get<WmsTransferOrderApi.TransferOrder>(
    `/wms/transfer-order/get?id=${id}`,
  );
}

/** 新增WMS-库存转移调拨单 */
export function createTransferOrder(data: WmsTransferOrderApi.TransferOrder) {
  return requestClient.post('/wms/transfer-order/create', data);
}

/** 修改WMS-库存转移调拨单 */
export function updateTransferOrder(data: WmsTransferOrderApi.TransferOrder) {
  return requestClient.put('/wms/transfer-order/update', data);
}

/** 删除WMS-库存转移调拨单 */
export function deleteTransferOrder(id: number) {
  return requestClient.delete(`/wms/transfer-order/delete?id=${id}`);
}

/** 批量删除WMS-库存转移调拨单 */
export function deleteTransferOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/transfer-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-库存转移调拨单 */
export function exportTransferOrder(params: any) {
  return requestClient.download('/wms/transfer-order/export-excel', { params });
}

/** 下载WMS-库存转移调拨单导入模板 */
export function importTransferOrderTemplate() {
  return requestClient.download('/wms/transfer-order/get-import-template');
}

/** 导入WMS-库存转移调拨单 */
export function importTransferOrder(file: File) {
  return requestClient.upload('/wms/transfer-order/import', { file });
}


// ==================== 子表（WMS-库存转移调拨明细） ====================

/** 获得WMS-库存转移调拨明细分页 */
export function getTransferOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsTransferOrderApi.TransferOrderLine>>(
    `/wms/transfer-order/transfer-order-line/page`,
    { params },
  );
}
/** 新增WMS-库存转移调拨明细 */
export function createTransferOrderLine(data: WmsTransferOrderApi.TransferOrderLine) {
  return requestClient.post(`/wms/transfer-order/transfer-order-line/create`, data);
}

/** 修改WMS-库存转移调拨明细 */
export function updateTransferOrderLine(data: WmsTransferOrderApi.TransferOrderLine) {
  return requestClient.put(`/wms/transfer-order/transfer-order-line/update`, data);
}

/** 删除WMS-库存转移调拨明细 */
export function deleteTransferOrderLine(id: number) {
  return requestClient.delete(`/wms/transfer-order/transfer-order-line/delete?id=${id}`);
}

/** 批量删除WMS-库存转移调拨明细 */
export function deleteTransferOrderLineList(ids: number[]) {
  return requestClient.delete(
    `/wms/transfer-order/transfer-order-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得WMS-库存转移调拨明细 */
export function getTransferOrderLine(id: number) {
  return requestClient.get<WmsTransferOrderApi.TransferOrderLine>(
    `/wms/transfer-order/transfer-order-line/get?id=${id}`,
  );
}
