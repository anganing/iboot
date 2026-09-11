import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesQcItemApi {
  /** MES-检验项目。方案编码与名称由模块内 JOIN 带出，前端只读。 */
  export interface QcItem {
    id?: number;
    planId?: number;
    planCode?: string;
    planName?: string;
    planStatus?: string;
    itemCode?: string;
    itemName?: string;
    itemType?: string;
    unit?: string;
    stdValue?: string;
    upperLimit?: number;
    lowerLimit?: number;
    requiredFlag?: boolean;
    sortNo?: number;
    status?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询检验项目分页 */
export function getQcItemPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcItemApi.QcItem>>(
    '/mes/qc-item/page',
    { params },
  );
}

/** 查询检验项目详情 */
export function getQcItem(id: number) {
  return requestClient.get<MesQcItemApi.QcItem>(`/mes/qc-item/get?id=${id}`);
}

/** 新增检验项目 */
export function createQcItem(data: MesQcItemApi.QcItem) {
  return requestClient.post('/mes/qc-item/create', data);
}

/** 修改检验项目 */
export function updateQcItem(data: MesQcItemApi.QcItem) {
  return requestClient.put('/mes/qc-item/update', data);
}

/** 删除检验项目 */
export function deleteQcItem(id: number) {
  return requestClient.delete(`/mes/qc-item/delete?id=${id}`);
}

/** 批量删除检验项目 */
export function deleteQcItemList(ids: number[]) {
  return requestClient.delete(`/mes/qc-item/delete-list?ids=${ids.join(',')}`);
}

/** 导出检验项目 */
export function exportQcItem(params: any) {
  return requestClient.download('/mes/qc-item/export-excel', { params });
}
