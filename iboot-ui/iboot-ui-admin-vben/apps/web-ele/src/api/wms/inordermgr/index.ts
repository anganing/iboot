import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsInOrderApi {
  /** 入库明细信息 */
  export interface InOrderLine {
    id?: number; // 主键，界面新建的行还没有主键
    orderId?: number; // 入库单编号
    lineNo?: number; // 行号
    sourceLineId?: number; // 来源明细编号
    sourceLineNo?: string; // 来源明细行号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec?: string; // 物料规格快照
    batchId?: number; // 批次编号，过账前可为空
    lotNo?: string; // 批次号快照
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    locId?: number; // 目标库位编号
    locCode?: string; // 目标库位编码快照
    containerId?: number; // 容器编号
    containerNo?: string; // 容器编号快照
    qualityStatus?: string; // 入库质量状态
    planQty?: number; // 计划入库数量
    completedQty?: number; // 已完成数量
    postedQty?: number; // 已过账数量
    qcTaskId?: number; // 质检任务编号
    status?: string; // 明细状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 入库单信息 */
  export interface InOrder {
    id: number; // 主键
    inNo?: string; // 入库单号
    bizType?: string; // 入库业务类型
    sourceType?: string; // 直接来源类型
    sourceBizNo?: string; // 直接来源业务单号
    sourceLineId?: number; // 直接来源明细编号
    originType?: string; // 原始来源类型
    originNo?: string; // 原始来源单号
    arrivalId?: number; // 到货登记编号
    supplierId?: number; // 供应商编号
    customerId?: number; // 客户编号
    prodOrderNo?: string; // 生产工单号快照
    workOrderNo?: string; // 作业工单号快照
    transferNo?: string; // 转移或调拨单号
    countOrderNo?: string; // 盘点单号
    invOrgId?: number; // 目标库存组织编号
    plantId?: number; // 目标工厂编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    whId?: number; // 目标仓库编号
    whCode?: string; // 目标仓库编码快照
    fromInvOrgId?: number; // 来源库存组织编号，0表示非调拨
    fromWhId?: number; // 来源仓库编号，0表示非调拨
    needQc?: boolean; // 是否需要质检
    iqcMode?: string; // 来料检验模式
    qualityStatus?: string; // 入库质量状态
    hookMode?: string; // 外部设备触发方式
    status?: string; // 状态
    idempotencyKey?: string; // 创建幂等键
    bizAttrsJson?: string; // 低频扩展属性
    remark?: string; // 备注或人工原因
    createTime?: string; // 创建时间
    arrivalArrival_no?: string; // 到货登记arrival_no
    whName?: string; // 目标仓库名称
    /**
     * 入库明细。
     *
     * 后端 createInOrder / updateInOrder 对空明细直接抛"明细不能为空"，所以明细随单头一次提交，
     * 改单时是整批替换（旧行全删后重建），前端必须把全部明细都带上，不能只传变动的行。
     */
    lines?: InOrderLine[];
  }

  /** 单据动作请求（提交、过账共用） */
  export interface OrderActionReq {
    id: number; // 入库单编号
    idempotencyKey?: string; // 幂等键，留空由服务端按单据编号与动作确定性派生
    remark?: string; // 备注
  }

  /** 单据取消请求 */
  export interface OrderCancelReq {
    id: number; // 入库单编号
    idempotencyKey?: string; // 幂等键，留空由服务端派生
    reason: string; // 取消原因，必填
  }
}

/** 查询入库单分页 */
export function getInOrderPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInOrderApi.InOrder>>(
    '/wms/in-order/page',
    { params },
  );
}

/** 查询入库单详情 */
export function getInOrder(id: number) {
  return requestClient.get<WmsInOrderApi.InOrder>(`/wms/in-order/get?id=${id}`);
}

/** 新增入库单，明细随单头一起提交 */
export function createInOrder(data: WmsInOrderApi.InOrder) {
  return requestClient.post('/wms/in-order/create', data);
}

/** 修改入库单，仅新建状态可改，明细整批替换 */
export function updateInOrder(data: WmsInOrderApi.InOrder) {
  return requestClient.put('/wms/in-order/update', data);
}

/** 删除入库单，仅新建状态可删 */
export function deleteInOrder(id: number) {
  return requestClient.delete(`/wms/in-order/delete?id=${id}`);
}

/** 批量删除入库单，仅新建状态可删 */
export function deleteInOrderList(ids: number[]) {
  return requestClient.delete(`/wms/in-order/delete-list?ids=${ids.join(',')}`);
}

/** 导出入库单 */
export function exportInOrder(params: any) {
  return requestClient.download('/wms/in-order/export-excel', { params });
}

// ==================== 领域动作 ====================

/** 提交入库单：新建推进到已确认 */
export function submitInOrder(data: WmsInOrderApi.OrderActionReq) {
  return requestClient.post('/wms/in-order/submit', data);
}

/** 过账入库单：写库存流水并增加余额 */
export function confirmInOrder(data: WmsInOrderApi.OrderActionReq) {
  return requestClient.post('/wms/in-order/confirm', data);
}

/** 取消入库单：仅新建状态可取消 */
export function cancelInOrder(data: WmsInOrderApi.OrderCancelReq) {
  return requestClient.post('/wms/in-order/cancel', data);
}

// ==================== 子表（入库明细，只读） ====================

/**
 * 获得入库明细分页。
 *
 * 明细的增删改端点已随写入路径收口撤销，明细只能经单据的 create / update 整单提交，
 * 这里只留查询：给已提交、已过账的单据看明细，以及改单时把现有明细读进表单。
 */
export function getInOrderLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsInOrderApi.InOrderLine>>(
    `/wms/in-order/in-order-line/page`,
    { params },
  );
}

/** 获得入库明细 */
export function getInOrderLine(id: number) {
  return requestClient.get<WmsInOrderApi.InOrderLine>(
    `/wms/in-order/in-order-line/get?id=${id}`,
  );
}
