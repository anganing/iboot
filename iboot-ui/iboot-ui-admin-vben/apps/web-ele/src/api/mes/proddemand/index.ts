import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesProdDemandApi {
  /**
   * MES-生产需求。
   *
   * 物料编码、名称、规格与计量单位编码是后端从 mdm 门面取的快照，工厂、项目、项目任务名称
   * 由 Service 走门面批量补全，前端只读不提交。
   */
  export interface ProdDemand {
    id?: number;
    demandNo?: string;
    sourceType?: string;
    sourceNo?: string;
    plantId?: number;
    plantName?: string;
    projectId?: number;
    projectName?: string;
    projectTaskId?: number;
    projectTaskName?: string;
    matId?: number;
    matCode?: string;
    matName?: string;
    matSpec?: string;
    qty?: number;
    uomId?: number;
    uomCode?: string;
    dueDate?: number | string;
    priority?: number;
    status?: string;
    idempotencyKey?: string;
    remark?: string;
    createTime?: string;
  }

  export interface ProdDemandActionReq {
    id: number;
    idempotencyKey?: string;
    reason?: string;
    remark?: string;
  }

  export interface ProdDemandToOrderReq {
    id: number;
    qty: number;
    workshopId?: number;
    idempotencyKey?: string;
    remark?: string;
  }
}

/** 查询生产需求分页 */
export function getProdDemandPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdDemandApi.ProdDemand>>(
    '/mes/prod-demand/page',
    { params },
  );
}

/** 查询生产需求详情 */
export function getProdDemand(id: number) {
  return requestClient.get<MesProdDemandApi.ProdDemand>(
    `/mes/prod-demand/get?id=${id}`,
  );
}

/** 新增生产需求 */
export function createProdDemand(data: MesProdDemandApi.ProdDemand) {
  return requestClient.post('/mes/prod-demand/create', data);
}

/** 修改生产需求 */
export function updateProdDemand(data: MesProdDemandApi.ProdDemand) {
  return requestClient.put('/mes/prod-demand/update', data);
}

/** 删除生产需求 */
export function deleteProdDemand(id: number) {
  return requestClient.delete(`/mes/prod-demand/delete?id=${id}`);
}

/** 批量删除生产需求 */
export function deleteProdDemandList(ids: number[]) {
  return requestClient.delete(
    `/mes/prod-demand/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出生产需求 */
export function exportProdDemand(params: any) {
  return requestClient.download('/mes/prod-demand/export-excel', { params });
}

/** 下载生产需求导入模板 */
export function importProdDemandTemplate() {
  return requestClient.download('/mes/prod-demand/get-import-template');
}

/** 导入生产需求 */
export function importProdDemand(file: File) {
  return requestClient.upload('/mes/prod-demand/import', { file });
}

/** 确认生产需求 */
export function confirmProdDemand(data: MesProdDemandApi.ProdDemandActionReq) {
  return requestClient.post('/mes/prod-demand/confirm', data);
}

/** 取消生产需求 */
export function cancelProdDemand(data: MesProdDemandApi.ProdDemandActionReq) {
  return requestClient.post('/mes/prod-demand/cancel', data);
}

/** 生产需求转生产工单 */
export function prodDemandToProdOrder(
  data: MesProdDemandApi.ProdDemandToOrderReq,
) {
  return requestClient.post<number>('/mes/prod-demand/to-prod-order', data);
}
