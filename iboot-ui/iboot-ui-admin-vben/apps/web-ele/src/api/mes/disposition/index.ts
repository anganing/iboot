import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesDispositionApi {
  /**
   * MES-不合格品处置单。
   *
   * 质检任务号与物料快照由模块内 JOIN 带出；审批人昵称由 Service 走 system 门面补全。
   */
  export interface Disposition {
    id?: number;
    dispositionNo?: string;
    taskId?: number;
    taskNo?: string;
    matCode?: string;
    matName?: string;
    dispositionType?: string;
    qty?: number;
    targetBizType?: string;
    targetBizNo?: string;
    approvedBy?: number;
    approvedByName?: string;
    approveTime?: number | string;
    status?: string;
    idempotencyKey?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询不合格品处置单分页 */
export function getDispositionPage(params: PageParam) {
  return requestClient.get<PageResult<MesDispositionApi.Disposition>>(
    '/mes/disposition/page',
    { params },
  );
}

/** 查询不合格品处置单详情 */
export function getDisposition(id: number) {
  return requestClient.get<MesDispositionApi.Disposition>(
    `/mes/disposition/get?id=${id}`,
  );
}

/** 新增不合格品处置单 */
export function createDisposition(data: MesDispositionApi.Disposition) {
  return requestClient.post('/mes/disposition/create', data);
}

/** 修改不合格品处置单 */
export function updateDisposition(data: MesDispositionApi.Disposition) {
  return requestClient.put('/mes/disposition/update', data);
}

/** 删除不合格品处置单 */
export function deleteDisposition(id: number) {
  return requestClient.delete(`/mes/disposition/delete?id=${id}`);
}

/** 批量删除不合格品处置单 */
export function deleteDispositionList(ids: number[]) {
  return requestClient.delete(
    `/mes/disposition/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出不合格品处置单 */
export function exportDisposition(params: any) {
  return requestClient.download('/mes/disposition/export-excel', { params });
}
