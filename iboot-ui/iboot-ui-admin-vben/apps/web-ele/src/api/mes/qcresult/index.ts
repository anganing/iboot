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

/** 批量保存检验结果 */
export function batchSaveQcResult(data: {
  taskId: number;
  inspectorId: number;
  items: {
    itemId: number;
    sampleNo: number;
    actualValue?: string;
  }[];
}) {
  return requestClient.post('/mes/qc-result/batch-save', data);
}

/** 导出检验结果 */
export function exportQcResult(params: any) {
  return requestClient.download('/mes/qc-result/export-excel', { params });
}
