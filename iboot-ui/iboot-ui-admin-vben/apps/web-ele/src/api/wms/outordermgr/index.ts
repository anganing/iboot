import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsOutOrderApi {
  /** 出库明细信息 */
  export interface OutOrderLine {
    id?: number; // 主键，界面新建的行还没有主键
    orderId?: number; // 出库单编号
    lineNo?: number; // 行号
    sourceLineId?: number; // 来源明细编号
    sourceLineNo?: string; // 来源明细行号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec?: string; // 物料规格快照
    batchId?: number; // 指定或分配的批次编号
    lotNo?: string; // 批次号快照
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    fromLocId?: number; // 来源库位编号
    fromLocCode?: string; // 来源库位编码快照
    toLocId?: number; // 目标库位编号
    toLocCode?: string; // 目标库位编码
    containerId?: number; // 容器编号
    containerNo?: string; // 容器编号快照
    qualityStatus?: string; // 要求的库存质量状态
    planQty?: number; // 计划出库数量
    allocatedQty?: number; // 已分配数量
    pickedQty?: number; // 已拣选数量
    postedQty?: number; // 已过账数量
    /**
     * 关联库存预留编号，0 表示无预留。
     *
     * MES 领料是"先建预留、再出库过账、过账时核销预留"三段，行上带着预留编号过账才能核销。
     * 界面手工建的出库单没有上游预留，恒为 0。
     */
    reservationId?: number;
    status?: string; // 明细状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 出库单信息 */
  export interface OutOrder {
    id: number; // 主键
    outNo?: string; // 出库单号
    bizType?: string; // 出库业务类型
    sourceType?: string; // 直接来源类型
    sourceBizNo?: string; // 直接来源业务单号
    sourceLineId?: number; // 直接来源明细编号
    originType?: string; // 原始来源类型
    originNo?: string; // 原始来源单号
    customerId?: number; // 客户编号
    supplierId?: number; // 供应商编号
    prodOrderNo?: string; // 生产工单号快照
    workOrderNo?: string; // 作业工单号快照
    opTaskId?: number; // 工序任务编号
    materialReqNo?: string; // 领退补料申请单号
    transferNo?: string; // 转移或调拨单号
    countOrderNo?: string; // 盘点单号
    invOrgId?: number; // 来源库存组织编号
    plantId?: number; // 来源工厂编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    whId?: number; // 来源仓库编号
    whCode?: string; // 来源仓库编码快照
    toInvOrgId?: number; // 目标库存组织编号，0表示非调拨
    toWhId?: number; // 目标仓库编号，0表示非调拨
    pickStrategyId?: number; // 拣选策略编号
    hookMode?: string; // 外部设备触发方式
    status?: string; // 状态
    idempotencyKey?: string; // 创建幂等键
    bizAttrsJson?: string; // 低频扩展属性
    remark?: string; // 备注或人工原因
    createTime?: string; // 创建时间
    whName?: string; // 来源仓库名称
    /**
     * 出库明细。
     *
     * 后端 createOutOrder / updateOutOrder 对空明细直接抛"明细不能为空"，所以明细随单头一次提交，
     * 改单时是整批替换（旧行全删后重建），前端必须把全部明细都带上，不能只传变动的行。
     */
    lines?: OutOrderLine[];
  }

  /** 单据动作请求（提交、过账共用） */
  export interface OrderActionReq {
    id: number; // 出库单编号
    idempotencyKey?: string; // 幂等键，留空由服务端按单据编号与动作确定性派生
    remark?: string; // 备注
  }

  /** 单据取消请求 */
  export interface OrderCancelReq {
    id: number; // 出库单编号
    idempotencyKey?: string; // 幂等键，留空由服务端派生
    reason: string; // 取消原因，必填
  }
}

/** 查询出库单分页 */
export function getOutOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsOutOrderApi.OutOrder>>(
    '/wms/out-order/page',
    { params },
  );
}

/** 查询出库单详情 */
export function getOutOrder(id: number) {
  return requestClient.get<WmsOutOrderApi.OutOrder>(
    `/wms/out-order/get?id=${id}`,
  );
}

/** 新增出库单，明细随单头一起提交 */
export function createOutOrder(data: WmsOutOrderApi.OutOrder) {
  return requestClient.post('/wms/out-order/create', data);
}

/** 修改出库单，仅新建状态可改，明细整批替换 */
export function updateOutOrder(data: WmsOutOrderApi.OutOrder) {
  return requestClient.put('/wms/out-order/update', data);
}

/** 删除出库单，仅新建状态可删 */
export function deleteOutOrder(id: number) {
  return requestClient.delete(`/wms/out-order/delete?id=${id}`);
}

/** 批量删除出库单，仅新建状态可删 */
export function deleteOutOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/out-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出出库单 */
export function exportOutOrder(params: any) {
  return requestClient.download('/wms/out-order/export-excel', { params });
}

// ==================== 领域动作 ====================

/** 提交出库单：新建推进到已确认 */
export function submitOutOrder(data: WmsOutOrderApi.OrderActionReq) {
  return requestClient.post('/wms/out-order/submit', data);
}

/** 过账出库单：写库存流水、扣减余额并核销行上预留 */
export function confirmOutOrder(data: WmsOutOrderApi.OrderActionReq) {
  return requestClient.post('/wms/out-order/confirm', data);
}

/** 取消出库单：仅新建状态可取消 */
export function cancelOutOrder(data: WmsOutOrderApi.OrderCancelReq) {
  return requestClient.post('/wms/out-order/cancel', data);
}

// ==================== 子表（出库明细，只读） ====================

/**
 * 获得出库明细分页。
 *
 * 明细的增删改端点已随写入路径收口撤销，明细只能经单据的 create / update 整单提交，
 * 这里只留查询：给已提交、已过账的单据看明细，以及改单时把现有明细读进表单。
 */
export function getOutOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsOutOrderApi.OutOrderLine>>(
    `/wms/out-order/out-order-line/page`,
    { params },
  );
}

/** 获得出库明细 */
export function getOutOrderLine(id: number) {
  return requestClient.get<WmsOutOrderApi.OutOrderLine>(
    `/wms/out-order/out-order-line/get?id=${id}`,
  );
}
