import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsTransferOrderApi {
  /** 调拨明细信息 */
  export interface TransferOrderLine {
    id?: number; // 主键，界面新建的行还没有主键
    orderId?: number; // 转移调拨单编号
    lineNo?: number; // 行号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    batchId?: number; // 批次编号
    lotNo?: string; // 批次号快照
    containerId?: number; // 容器编号，0表示散件
    containerNo?: string; // 容器编号快照
    fromLocId?: number; // 来源库位编号
    fromLocCode?: string; // 来源库位编码
    toLocId?: number; // 目标库位编号
    toLocCode?: string; // 目标库位编码
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
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 移库调拨单信息 */
  export interface TransferOrder {
    id: number; // 主键
    transferNo?: string; // 转移或调拨单号
    transferType?: string; // 转移类型
    sourceType?: string; // 来源业务类型
    sourceBizNo?: string; // 来源业务单号
    fromInvOrgId?: number; // 来源库存组织编号
    toInvOrgId?: number; // 目标库存组织编号
    fromWhId?: number; // 来源仓库编号
    toWhId?: number; // 目标仓库编号
    fromProjectId?: number; // 来源项目编号，0表示公共库存
    toProjectId?: number; // 目标项目编号，0表示公共库存
    hookMode?: string; // 外部设备触发方式
    status?: string; // 状态
    idempotencyKey?: string; // 创建幂等键
    remark?: string; // 备注
    createTime?: string; // 创建时间
    fromWhCode?: string; // 来源仓库编码
    fromWhName?: string; // 来源仓库名称
    toWhCode?: string; // 目标仓库编码
    toWhName?: string; // 目标仓库名称
    /**
     * 转移明细。
     *
     * 后端 createTransferOrder / updateTransferOrder 对空明细直接抛"明细不能为空"，所以明细随单头
     * 一次提交，改单时是整批替换（旧行全删后重建），前端必须把全部明细都带上。
     *
     * 物料编码与名称快照要前端带过来：转移单领域侧不回填物料快照，不带库存流水上的物料编码就是空的。
     */
    lines?: TransferOrderLine[];
  }

  /** 单据动作请求（提交、过账共用） */
  export interface OrderActionReq {
    id: number; // 移库调拨单编号
    idempotencyKey?: string; // 幂等键，留空由服务端按单据编号与动作确定性派生
    remark?: string; // 备注
  }

  /** 单据取消请求 */
  export interface OrderCancelReq {
    id: number; // 移库调拨单编号
    idempotencyKey?: string; // 幂等键，留空由服务端派生
    reason: string; // 取消原因，必填
  }
}

/** 查询移库调拨单分页 */
export function getTransferOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsTransferOrderApi.TransferOrder>>(
    '/wms/transfer-order/page',
    { params },
  );
}

/** 查询移库调拨单详情 */
export function getTransferOrder(id: number) {
  return requestClient.get<WmsTransferOrderApi.TransferOrder>(
    `/wms/transfer-order/get?id=${id}`,
  );
}

/** 新增移库调拨单，明细随单头一起提交 */
export function createTransferOrder(data: WmsTransferOrderApi.TransferOrder) {
  return requestClient.post('/wms/transfer-order/create', data);
}

/** 修改移库调拨单，仅新建状态可改，明细整批替换 */
export function updateTransferOrder(data: WmsTransferOrderApi.TransferOrder) {
  return requestClient.put('/wms/transfer-order/update', data);
}

/** 删除移库调拨单，仅新建状态可删 */
export function deleteTransferOrder(id: number) {
  return requestClient.delete(`/wms/transfer-order/delete?id=${id}`);
}

/** 批量删除移库调拨单，仅新建状态可删 */
export function deleteTransferOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/transfer-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出移库调拨单 */
export function exportTransferOrder(params: any) {
  return requestClient.download('/wms/transfer-order/export-excel', { params });
}

// ==================== 领域动作 ====================

/** 提交移库调拨单：新建推进到已确认 */
export function submitTransferOrder(
  data: WmsTransferOrderApi.OrderActionReq,
) {
  return requestClient.post('/wms/transfer-order/submit', data);
}

/** 过账移库调拨单：来源维度扣减、目标维度增加 */
export function confirmTransferOrder(
  data: WmsTransferOrderApi.OrderActionReq,
) {
  return requestClient.post('/wms/transfer-order/confirm', data);
}

/** 取消移库调拨单：仅新建状态可取消 */
export function cancelTransferOrder(
  data: WmsTransferOrderApi.OrderCancelReq,
) {
  return requestClient.post('/wms/transfer-order/cancel', data);
}

// ==================== 子表（调拨明细，只读） ====================

/**
 * 获得调拨明细分页。
 *
 * 明细的增删改端点已随写入路径收口撤销，明细只能经单据的 create / update 整单提交，
 * 这里只留查询：给已提交、已过账的单据看明细，以及改单时把现有明细读进表单。
 */
export function getTransferOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsTransferOrderApi.TransferOrderLine>>(
    `/wms/transfer-order/transfer-order-line/page`,
    { params },
  );
}

/** 获得调拨明细 */
export function getTransferOrderLine(id: number) {
  return requestClient.get<WmsTransferOrderApi.TransferOrderLine>(
    `/wms/transfer-order/transfer-order-line/get?id=${id}`,
  );
}
