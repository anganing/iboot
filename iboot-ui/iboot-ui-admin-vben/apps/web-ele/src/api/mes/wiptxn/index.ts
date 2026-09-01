import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesWipTxnApi {
  /**
   * MES-在制流转流水。
   *
   * 流水是过账留痕的证据链，只提供查询与导出，没有新增/修改/删除接口。
   */
  export interface WipTxn {
    id?: number;
    txnNo?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    opName?: string;
    txnType?: string;
    qty?: number;
    fromStatus?: string;
    toStatus?: string;
    bizType?: string;
    bizNo?: string;
    fromOpTaskId?: number;
    fromOpTaskNo?: string;
    toOpTaskId?: number;
    toOpTaskNo?: string;
    txnTime?: string;
    idempotencyKey?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询在制流转流水分页 */
export function getWipTxnPage(params: PageParam) {
  return requestClient.get<PageResult<MesWipTxnApi.WipTxn>>(
    '/mes/wip-txn/page',
    { params },
  );
}

/** 查询在制流转流水详情 */
export function getWipTxn(id: number) {
  return requestClient.get<MesWipTxnApi.WipTxn>(`/mes/wip-txn/get?id=${id}`);
}

/** 导出在制流转流水 */
export function exportWipTxn(params: any) {
  return requestClient.download('/mes/wip-txn/export-excel', { params });
}
