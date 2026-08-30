import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesWipTxnApi {
  /** MES-在制流转流水信息 */
  export interface WipTxn {
    id: number; // 主键
    txnNo?: string; // 在制事务编号
    prodOrderId?: number; // 生产工单编号
    workOrderId?: number; // 作业工单编号
    opTaskId?: number; // 工序任务编号
    txnType?: string; // 事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP
    qty?: number; // 事务数量
    fromStatus: string; // 变更前状态
    toStatus?: string; // 变更后状态
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    fromOpTaskId?: number; // 来源工序任务编号，0表示无
    toOpTaskId?: number; // 目标工序任务编号，0表示无
    txnTime?: string | Dayjs; // 事务发生时间
    idempotencyKey?: string; // 事务幂等键
    remark: string; // 备注
  }
}

/** 查询MES-在制流转流水分页 */
export function getWipTxnPage(params: PageParam) {
  return requestClient.get<PageResult<MesWipTxnApi.WipTxn>>(
    '/mes/wip-txn/page',
    { params },
  );
}

/** 查询MES-在制流转流水详情 */
export function getWipTxn(id: number) {
  return requestClient.get<MesWipTxnApi.WipTxn>(
    `/mes/wip-txn/get?id=${id}`,
  );
}

/** 新增MES-在制流转流水 */
export function createWipTxn(data: MesWipTxnApi.WipTxn) {
  return requestClient.post('/mes/wip-txn/create', data);
}

/** 修改MES-在制流转流水 */
export function updateWipTxn(data: MesWipTxnApi.WipTxn) {
  return requestClient.put('/mes/wip-txn/update', data);
}

/** 删除MES-在制流转流水 */
export function deleteWipTxn(id: number) {
  return requestClient.delete(`/mes/wip-txn/delete?id=${id}`);
}

/** 批量删除MES-在制流转流水 */
export function deleteWipTxnList(ids: number[]) {
  return requestClient.delete(
    `/mes/wip-txn/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-在制流转流水 */
export function exportWipTxn(params: any) {
  return requestClient.download('/mes/wip-txn/export-excel', { params });
}

/** 下载MES-在制流转流水导入模板 */
export function importWipTxnTemplate() {
  return requestClient.download('/mes/wip-txn/get-import-template');
}

/** 导入MES-在制流转流水 */
export function importWipTxn(file: File) {
  return requestClient.upload('/mes/wip-txn/import', { file });
}

