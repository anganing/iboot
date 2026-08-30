import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsInvTxnApi {
  /** 库存流水信息 */
  export interface InvTxn {
    id: number; // 主键
    txnNo?: string; // 库存事务编号
    txnType?: string; // 事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    bizLineNo: string; // 来源业务行号
    ioType?: string; // 收发方向：IN、OUT、MOVE、STATUS
    invOrgId?: number; // 库存组织编号
    plantId?: number; // 工厂编号
    whId?: number; // 仓库编号
    fromLocId?: number; // 来源库位编号，0表示无来源库位
    toLocId?: number; // 目标库位编号，0表示无目标库位
    matId?: number; // 物料编号
    batchId?: number; // 批次编号
    containerId?: number; // 容器编号，0表示散件
    uomId?: number; // 基准计量单位编号
    qty?: number; // 事务数量，使用正数
    beforeQty?: number; // 过账前现存数量
    afterQty?: number; // 过账后现存数量
    qualityStatusFrom: string; // 变更前质量状态
    qualityStatusTo: string; // 变更后质量状态
    projectIdFrom?: number; // 变更前项目编号，0表示无项目
    projectTaskIdFrom?: number; // 变更前项目任务编号，0表示无项目任务
    projectIdTo?: number; // 变更后项目编号，0表示无项目
    projectTaskIdTo?: number; // 变更后项目任务编号，0表示无项目任务
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    uomCode?: string; // 单位编码快照
    lotNo?: string; // 批次号快照
    fromLocCode: string; // 来源库位编码快照
    toLocCode: string; // 目标库位编码快照
    idempotencyKey?: string; // 过账幂等键
    remark: string; // 备注
  }
}

/** 查询库存流水分页 */
export function getInvTxnPage(params: PageParam) {
  return requestClient.get<PageResult<WmsInvTxnApi.InvTxn>>(
    '/wms/inv-txn/page',
    { params },
  );
}

/** 查询库存流水详情 */
export function getInvTxn(id: number) {
  return requestClient.get<WmsInvTxnApi.InvTxn>(
    `/wms/inv-txn/get?id=${id}`,
  );
}

/** 新增库存流水 */
export function createInvTxn(data: WmsInvTxnApi.InvTxn) {
  return requestClient.post('/wms/inv-txn/create', data);
}

/** 修改库存流水 */
export function updateInvTxn(data: WmsInvTxnApi.InvTxn) {
  return requestClient.put('/wms/inv-txn/update', data);
}

/** 删除库存流水 */
export function deleteInvTxn(id: number) {
  return requestClient.delete(`/wms/inv-txn/delete?id=${id}`);
}

/** 批量删除库存流水 */
export function deleteInvTxnList(ids: number[]) {
  return requestClient.delete(
    `/wms/inv-txn/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出库存流水 */
export function exportInvTxn(params: any) {
  return requestClient.download('/wms/inv-txn/export-excel', { params });
}

/** 下载库存流水导入模板 */
export function importInvTxnTemplate() {
  return requestClient.download('/wms/inv-txn/get-import-template');
}

/** 导入库存流水 */
export function importInvTxn(file: File) {
  return requestClient.upload('/wms/inv-txn/import', { file });
}

