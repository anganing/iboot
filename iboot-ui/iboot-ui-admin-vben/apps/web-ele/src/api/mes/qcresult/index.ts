import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesQcResultApi {
  /**
   * MES-检验结果。
   *
   * 检验项编码与名称是快照，由后端从检验项目带出；检验员昵称由 Service 走 system 门面补全。
   */
  export interface QcResult {
    id?: number;
    taskId?: number;
    taskNo?: string;
    itemId?: number;
    sampleNo?: number;
    itemCode?: string;
    itemName?: string;
    actualValue?: string;
    passFlag?: boolean;
    inspectorId?: number;
    inspectorName?: string;
    inspectTime?: number | string;
    qtyQualified?: number;
    qtyRejected?: number;
    conclusion?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询检验结果分页 */
export function getQcResultPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcResultApi.QcResult>>(
    '/mes/qc-result/page',
    { params },
  );
}

/** 查询检验结果详情 */
export function getQcResult(id: number) {
  return requestClient.get<MesQcResultApi.QcResult>(
    `/mes/qc-result/get?id=${id}`,
  );
}

/** 新增检验结果 */
export function createQcResult(data: MesQcResultApi.QcResult) {
  return requestClient.post('/mes/qc-result/create', data);
}

/** 修改检验结果 */
export function updateQcResult(data: MesQcResultApi.QcResult) {
  return requestClient.put('/mes/qc-result/update', data);
}

/** 删除检验结果 */
export function deleteQcResult(id: number) {
  return requestClient.delete(`/mes/qc-result/delete?id=${id}`);
}

/** 批量删除检验结果 */
export function deleteQcResultList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-result/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出检验结果 */
export function exportQcResult(params: any) {
  return requestClient.download('/mes/qc-result/export-excel', { params });
}
