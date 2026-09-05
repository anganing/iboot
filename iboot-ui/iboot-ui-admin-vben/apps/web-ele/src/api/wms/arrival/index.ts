import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsArrivalApi {
  /** 到货明细信息 */
  export interface ArrivalLine {
    id?: number; // 主键，界面新建的行还没有主键
    arrivalId?: number; // 到货登记编号
    lineNo?: number; // 行号
    sourceLineNo?: string; // 来源单据行号
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec?: string; // 物料规格快照
    lotNo?: string; // 到货批次号
    supplierLotNo?: string; // 供应商批次号
    arrivalQty?: number; // 到货数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    needQc?: boolean; // 是否需要质检
    iqcMode?: string; // 来料检验模式
    qcTaskId?: number; // 质检任务编号
    qualifiedQty?: number; // 合格数量
    rejectedQty?: number; // 不合格数量
    status?: string; // 明细状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 到货登记信息 */
  export interface Arrival {
    id: number; // 主键
    arrivalNo?: string; // 到货登记单号
    sourceType?: string; // 来源类型
    sourceNo?: string; // 来源单号
    supplierId?: number; // 供应商编号
    supplierCode?: string; // 供应商编码快照
    supplierName?: string; // 供应商名称快照
    invOrgId?: number; // 库存组织编号
    plantId?: number; // 工厂编号
    whId?: number; // 目标仓库编号
    expectDate?: string; // 预计到货日期
    arrivalTime?: string; // 实际到货时间，由确认到货动作写入
    status?: string; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 目标仓库编码
    whName?: string; // 目标仓库名称
    /**
     * 到货明细。
     *
     * 后端 createArrival / updateArrival 对空明细直接抛"明细不能为空"，所以明细随单头一次提交，
     * 改单时是整批替换（旧行全删后重建），前端必须把全部明细都带上。
     */
    lines?: ArrivalLine[];
  }

  /** 到货确认请求 */
  export interface ArrivalActionReq {
    id: number; // 到货登记编号
    idempotencyKey?: string; // 幂等键，留空由服务端按单据编号与动作确定性派生
    remark?: string; // 备注
  }

  /** 按待入库明细生成入库单的请求 */
  export interface ArrivalCreateInOrderReq {
    id: number; // 到货登记编号
    idempotencyKey?: string; // 幂等键，留空由服务端派生
    lineIds?: number[]; // 要生成入库单的明细编号，留空表示全部待入库明细
  }
}

/** 查询到货登记分页 */
export function getArrivalPage(params: PageParam) {
  return requestClient.get<PageResult<WmsArrivalApi.Arrival>>(
    '/wms/arrival/page',
    { params },
  );
}

/** 查询到货登记详情 */
export function getArrival(id: number) {
  return requestClient.get<WmsArrivalApi.Arrival>(`/wms/arrival/get?id=${id}`);
}

/** 新增到货登记，明细随单头一起提交 */
export function createArrival(data: WmsArrivalApi.Arrival) {
  return requestClient.post('/wms/arrival/create', data);
}

/** 修改到货登记，仅草稿状态可改，明细整批替换 */
export function updateArrival(data: WmsArrivalApi.Arrival) {
  return requestClient.put('/wms/arrival/update', data);
}

/** 删除到货登记，仅草稿状态可删 */
export function deleteArrival(id: number) {
  return requestClient.delete(`/wms/arrival/delete?id=${id}`);
}

/** 批量删除到货登记，仅草稿状态可删 */
export function deleteArrivalList(ids: number[]) {
  return requestClient.delete(`/wms/arrival/delete-list?ids=${ids.join(',')}`);
}

/** 导出到货登记 */
export function exportArrival(params: any) {
  return requestClient.download('/wms/arrival/export-excel', { params });
}

// ==================== 领域动作 ====================

/**
 * 确认到货：推进到已到货、逐行注册批次、按是否需要质检把明细分流为待质检或待入库。
 *
 * 库存不在这一步动，要等生成的入库单过账。
 */
export function confirmArrival(data: WmsArrivalApi.ArrivalActionReq) {
  return requestClient.post('/wms/arrival/confirm', data);
}

/** 按待入库明细生成采购入库单，生成后明细置已完成 */
export function createInOrderFromArrival(
  data: WmsArrivalApi.ArrivalCreateInOrderReq,
) {
  return requestClient.post<number>('/wms/arrival/create-in-order', data);
}

// ==================== 子表（到货明细，只读） ====================

/**
 * 获得到货明细分页。
 *
 * 明细的增删改端点已随写入路径收口撤销，明细只能经单据的 create / update 整单提交，
 * 这里只留查询：给已确认到货的单据看明细，以及改单时把现有明细读进表单。
 */
export function getArrivalLinePage(params: PageParam) {
  return requestClient.get<PageResult<WmsArrivalApi.ArrivalLine>>(
    `/wms/arrival/arrival-line/page`,
    { params },
  );
}

/** 获得到货明细 */
export function getArrivalLine(id: number) {
  return requestClient.get<WmsArrivalApi.ArrivalLine>(
    `/wms/arrival/arrival-line/get?id=${id}`,
  );
}
