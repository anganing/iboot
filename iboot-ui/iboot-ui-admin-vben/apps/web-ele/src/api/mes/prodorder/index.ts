import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesProdOrderApi {
  /**
   * MES-生产工单。
   *
   * 物料与单位快照、BOM 与工艺路线版本由后端从 mdm 门面取值补全；已完成、报废数量由报工推进；
   * 齐套状态由齐套检查写入；下达与实际起止时间由业务动作写入。这些字段前端只读。
   */
  export interface ProdOrder {
    id?: number;
    orderNo?: string;
    demandId?: number;
    demandNo?: string;
    plantId?: number;
    plantName?: string;
    workshopId?: number;
    workshopName?: string;
    projectId?: number;
    projectName?: string;
    projectTaskId?: number;
    projectTaskName?: string;
    matId?: number;
    matCode?: string;
    matName?: string;
    matSpec?: string;
    qty?: number;
    completedQty?: number;
    scrapQty?: number;
    uomId?: number;
    uomCode?: string;
    bomId?: number;
    bomVersion?: string;
    routeId?: number;
    routeVersion?: string;
    dueDate?: number | string;
    priority?: number;
    kitStatus?: string;
    status?: string;
    releaseTime?: string;
    actualStartTime?: string;
    actualEndTime?: string;
    version?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询生产工单分页 */
export function getProdOrderPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdOrderApi.ProdOrder>>(
    '/mes/prod-order/page',
    { params },
  );
}

/** 查询生产工单详情 */
export function getProdOrder(id: number) {
  return requestClient.get<MesProdOrderApi.ProdOrder>(
    `/mes/prod-order/get?id=${id}`,
  );
}

/** 新增生产工单 */
export function createProdOrder(data: MesProdOrderApi.ProdOrder) {
  return requestClient.post('/mes/prod-order/create', data);
}

/** 修改生产工单 */
export function updateProdOrder(data: MesProdOrderApi.ProdOrder) {
  return requestClient.put('/mes/prod-order/update', data);
}

/** 删除生产工单 */
export function deleteProdOrder(id: number) {
  return requestClient.delete(`/mes/prod-order/delete?id=${id}`);
}

/** 批量删除生产工单 */
export function deleteProdOrderList(ids: number[]) {
  return requestClient.delete(
    `/mes/prod-order/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出生产工单 */
export function exportProdOrder(params: any) {
  return requestClient.download('/mes/prod-order/export-excel', { params });
}

/** 下载生产工单导入模板 */
export function importProdOrderTemplate() {
  return requestClient.download('/mes/prod-order/get-import-template');
}

/** 导入生产工单 */
export function importProdOrder(file: File) {
  return requestClient.upload('/mes/prod-order/import', { file });
}
