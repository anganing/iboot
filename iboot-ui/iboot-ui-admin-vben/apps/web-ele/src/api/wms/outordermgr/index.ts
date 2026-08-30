import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsOutOrderApi {
  /** WMS-出库单明细信息 */
  export interface OutOrderLine {
            id: number; // 主键
            orderId?: number; // 出库单编号
            lineNo?: number; // 行号
            sourceLineId: number; // 来源明细编号
            sourceLineNo: string; // 来源明细行号
            matId?: number; // 物料编号
            matCode?: string; // 物料编码快照
            matName?: string; // 物料名称快照
            matSpec: string; // 物料规格快照
            batchId: number; // 指定或分配的批次编号
            lotNo: string; // 批次号快照
            uomId?: number; // 计量单位编号
            uomCode?: string; // 计量单位编码快照
            fromLocId: number; // 来源库位编号
            fromLocCode: string; // 来源库位编码快照
            toLocId: number; // 目标库位编号
            qualityStatus?: string; // 要求的库存质量状态
            planQty?: number; // 计划出库数量
            allocatedQty?: number; // 已分配数量
            pickedQty?: number; // 已拣选数量
            postedQty?: number; // 已过账数量
            containerId: number; // 容器编号
            status?: string; // 明细状态
            remark: string; // 备注
  }

  /** WMS-出库单信息 */
  export interface OutOrder {
    id: number; // 主键
    outNo?: string; // 出库单号
    bizType?: string; // 出库业务类型：PROD_ISSUE_OUT、SALES_DELIVERY_OUT、PO_RETURN_OUT、SCRAP_OUT、TRANSFER_OUT、COUNT_LOSS_OUT、OTHER_OUT
    sourceType: string; // 直接来源类型
    sourceBizNo: string; // 直接来源业务单号
    sourceLineId: number; // 直接来源明细编号
    originType: string; // 原始来源类型
    originNo: string; // 原始来源单号
    customerId: number; // 客户编号
    supplierId: number; // 供应商编号
    prodOrderNo: string; // 生产工单号快照
    workOrderNo: string; // 作业工单号快照
    opTaskId: number; // 工序任务编号
    materialReqNo: string; // 领退补料申请单号
    transferNo: string; // 转移或调拨单号
    countOrderNo: string; // 盘点单号
    invOrgId?: number; // 来源库存组织编号
    plantId?: number; // 来源工厂编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    whId?: number; // 来源仓库编号
    whCode?: string; // 来源仓库编码快照
    toInvOrgId?: number; // 目标库存组织编号，0表示非调拨
    toWhId?: number; // 目标仓库编号，0表示非调拨
    pickStrategyId: number; // 拣选策略编号
    hookMode?: string; // 外部设备触发方式：NONE、WCS、AGV
    status?: string; // 状态：DRAFT、SUBMITTED、READY、TASK_CREATED、IN_PROGRESS、DONE、CANCELLED、EXCEPTION
    idempotencyKey: string; // 创建幂等键
    bizAttrsJson: string; // 低频扩展属性
    remark: string; // 备注或人工原因
  }
}

/** 查询WMS-出库单分页 */
export function getOutOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsOutOrderApi.OutOrder>>(
    '/wms/out-order/page',
    { params },
  );
}

/** 查询WMS-出库单详情 */
export function getOutOrder(id: number) {
  return requestClient.get<WmsOutOrderApi.OutOrder>(
    `/wms/out-order/get?id=${id}`,
  );
}

/** 新增WMS-出库单 */
export function createOutOrder(data: WmsOutOrderApi.OutOrder) {
  return requestClient.post('/wms/out-order/create', data);
}

/** 修改WMS-出库单 */
export function updateOutOrder(data: WmsOutOrderApi.OutOrder) {
  return requestClient.put('/wms/out-order/update', data);
}

/** 删除WMS-出库单 */
export function deleteOutOrder(id: number) {
  return requestClient.delete(`/wms/out-order/delete?id=${id}`);
}

/** 批量删除WMS-出库单 */
export function deleteOutOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/out-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-出库单 */
export function exportOutOrder(params: any) {
  return requestClient.download('/wms/out-order/export-excel', { params });
}

/** 下载WMS-出库单导入模板 */
export function importOutOrderTemplate() {
  return requestClient.download('/wms/out-order/get-import-template');
}

/** 导入WMS-出库单 */
export function importOutOrder(file: File) {
  return requestClient.upload('/wms/out-order/import', { file });
}


// ==================== 子表（WMS-出库单明细） ====================

/** 获得WMS-出库单明细分页 */
export function getOutOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsOutOrderApi.OutOrderLine>>(
    `/wms/out-order/out-order-line/page`,
    { params },
  );
}
/** 新增WMS-出库单明细 */
export function createOutOrderLine(data: WmsOutOrderApi.OutOrderLine) {
  return requestClient.post(`/wms/out-order/out-order-line/create`, data);
}

/** 修改WMS-出库单明细 */
export function updateOutOrderLine(data: WmsOutOrderApi.OutOrderLine) {
  return requestClient.put(`/wms/out-order/out-order-line/update`, data);
}

/** 删除WMS-出库单明细 */
export function deleteOutOrderLine(id: number) {
  return requestClient.delete(`/wms/out-order/out-order-line/delete?id=${id}`);
}

/** 批量删除WMS-出库单明细 */
export function deleteOutOrderLineList(ids: number[]) {
  return requestClient.delete(
    `/wms/out-order/out-order-line/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得WMS-出库单明细 */
export function getOutOrderLine(id: number) {
  return requestClient.get<WmsOutOrderApi.OutOrderLine>(
    `/wms/out-order/out-order-line/get?id=${id}`,
  );
}
