import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsCountOrderApi {
  /** 盘点明细信息 */
  export interface CountOrderLine {
    id: number; // 主键
    orderId?: number; // 盘点单编号
    lineNo?: number; // 行号
    balanceId?: number; // 库存余额编号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    batchId?: number; // 批次编号
    lotNo?: string; // 批次号快照
    locId?: number; // 库位编号
    locCode?: string; // 库位编码快照
    containerId?: number; // 容器编号，0表示散件
    containerNo?: string; // 容器编号快照
    qualityStatus?: string; // 质量状态
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    bookQty?: number; // 账面数量
    actualQty?: number; // 实盘数量
    diffQty?: number; // 差异数量
    adjustBizNo?: string; // 差异调整入出库单号
    status?: string; // 明细状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 盘点单信息 */
  export interface CountOrder {
    id: number; // 主键
    countNo?: string; // 盘点单号
    countType?: string; // 盘点类型
    invOrgId?: number; // 库存组织编号
    whId?: number; // 仓库编号
    zoneId?: number; // 库区编号，0表示全部库区
    projectId?: number; // 项目编号，0表示不限项目
    snapshotTime?: number | string; // 账面快照时间
    blindCountFlag?: boolean; // 是否盲盘
    status?: string; // 状态
    idempotencyKey?: string; // 创建幂等键
    remark?: string; // 备注
    approvalStatus?: number; // 流程审批状态
    processInstanceId?: string; // 流程实例编号
    approvalTime?: number | string; // 审批完成时间
    approvalReason?: string; // 审批意见或驳回原因
    createTime?: string; // 创建时间
    whCode?: string; // 仓库编码
    whName?: string; // 仓库名称
    zoneCode?: string; // 库区编号，0表示全部库区编码
    zoneName?: string; // 库区编号，0表示全部库区名称
    /**
     * 限定盘点物料范围，留空表示不限物料。
     *
     * 盘点单没有明细入参：盘点行由后端按仓库 + 库区 + 这个物料集合圈选库存余额自动生成，
     * 账面数量取当时的余额快照。抽盘就靠这个字段收窄范围。
     */
    matIds?: number[];
  }

  /** 盘点单动作请求（冻结、确认差异共用） */
  export interface CountActionReq {
    id: number; // 盘点单编号
    idempotencyKey?: string; // 幂等键，留空由服务端按单据编号与动作确定性派生
    remark?: string; // 备注
  }

  /** 实盘结果行 */
  export interface CountResultLine {
    lineId: number; // 盘点明细编号
    actualQty: number; // 实盘数量，允许为 0
    remark?: string; // 备注
  }

  /** 提交实盘结果请求，一次提交多行 */
  export interface CountResultSubmitReq {
    id: number; // 盘点单编号
    idempotencyKey?: string; // 幂等键，留空由服务端派生
    results: CountResultLine[]; // 实盘结果，差异数量由服务端计算
  }
}

/** 查询盘点单分页 */
export function getCountOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsCountOrderApi.CountOrder>>(
    '/wms/count-order/page',
    { params },
  );
}

/** 查询盘点单详情 */
export function getCountOrder(id: number) {
  return requestClient.get<WmsCountOrderApi.CountOrder>(
    `/wms/count-order/get?id=${id}`,
  );
}

/** 新增盘点单，盘点行由服务端按盘点范围生成 */
export function createCountOrder(data: WmsCountOrderApi.CountOrder) {
  return requestClient.post('/wms/count-order/create', data);
}

/** 修改盘点单，仅新建状态可改，改盘点范围会重新生成盘点行 */
export function updateCountOrder(data: WmsCountOrderApi.CountOrder) {
  return requestClient.put('/wms/count-order/update', data);
}

/** 删除盘点单，仅新建状态可删 */
export function deleteCountOrder(id: number) {
  return requestClient.delete(`/wms/count-order/delete?id=${id}`);
}

/** 批量删除盘点单，仅新建状态可删 */
export function deleteCountOrderList(ids: number[]) {
  return requestClient.delete(
    `/wms/count-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出盘点单 */
export function exportCountOrder(params: any) {
  return requestClient.download('/wms/count-order/export-excel', { params });
}

// ==================== 领域动作 ====================

/** 冻结盘点范围：逐行按账面数量做冻结过账，盘点期间这批货不许动 */
export function freezeCountOrder(data: WmsCountOrderApi.CountActionReq) {
  return requestClient.post('/wms/count-order/freeze', data);
}

/** 提交实盘结果：一次提交多行，差异数量由服务端按账面数量算 */
export function submitCountResult(data: WmsCountOrderApi.CountResultSubmitReq) {
  return requestClient.post('/wms/count-order/submit-result', data);
}

/** 确认盘点差异：生成盘盈入库单或盘亏出库单并过账 */
export function confirmCountDiff(data: WmsCountOrderApi.CountActionReq) {
  return requestClient.post('/wms/count-order/confirm-diff', data);
}

/** 提交盘点差异审批：审批通过后才生成调整出入库单并过账 */
export function submitDiffApproval(data: WmsCountOrderApi.CountActionReq) {
  return requestClient.post('/wms/count-order/submit-diff-approval', data);
}

// ==================== 子表（盘点明细，只读 + 录实盘） ====================

/**
 * 获得盘点明细分页。
 *
 * 明细的增删改端点已随写入路径收口撤销：账面数量是账实对比的基准，人工改它等于给账实一致留后门。
 * 实盘数量只能经 submitCountResult 批量录入。
 *
 * 参数类型与其他明细分页端点保持一致（PageParam，父键由调用方必传），
 * 但父键 orderId 是雪花 ID，超出 JS 安全整数时后端下发字符串，只能原样透传，
 * 转成 Number 会丢精度。
 */
export function getCountOrderLinePage(
  params: PageParam & { orderId?: number | string },
) {
  return requestClient.get<PageResult<WmsCountOrderApi.CountOrderLine>>(
    `/wms/count-order/count-order-line/page`,
    { params },
  );
}

/** 获得盘点明细 */
export function getCountOrderLine(id: number) {
  return requestClient.get<WmsCountOrderApi.CountOrderLine>(
    `/wms/count-order/count-order-line/get?id=${id}`,
  );
}
