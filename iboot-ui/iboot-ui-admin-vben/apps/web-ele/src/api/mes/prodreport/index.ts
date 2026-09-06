import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesProdReportApi {
  /**
   * MES-生产报工记录。
   *
   * 生产工单与作业工单编号由后端从工序任务反查带出；报工人、资源、班次名称由 Service 走门面补全。
   */
  export interface ProdReport {
    id?: number;
    reportNo?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    opName?: string;
    workerId?: number;
    workerName?: string;
    resId?: number;
    resName?: string;
    shiftId?: number;
    shiftName?: string;
    qtyGood?: number;
    qtyScrap?: number;
    qtyRework?: number;
    startTime?: number | string;
    endTime?: number | string;
    reportTime?: number | string;
    status?: string;
    idempotencyKey?: string;
    remark?: string;
    createTime?: string;
  }

  export interface ProdReportSubmitReq {
    opTaskId: number;
    idempotencyKey: string;
    qtyGood?: number;
    qtyScrap?: number;
    qtyRework?: number;
    workerId?: number;
    resId?: number;
    shiftId?: number;
    startTime?: string;
    endTime?: string;
    reportTime?: string;
    remark?: string;
  }

  export interface ProdReportCancelReq {
    id: number;
    idempotencyKey?: string;
    reason?: string;
  }
}

/** 查询生产报工分页 */
export function getProdReportPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdReportApi.ProdReport>>(
    '/mes/prod-report/page',
    { params },
  );
}

/** 查询生产报工详情 */
export function getProdReport(id: number) {
  return requestClient.get<MesProdReportApi.ProdReport>(
    `/mes/prod-report/get?id=${id}`,
  );
}

/** 提交生产报工 */
export function submitProdReport(
  data: MesProdReportApi.ProdReportSubmitReq,
) {
  return requestClient.post('/mes/prod-report/create', data);
}

/** 导出生产报工 */
export function exportProdReport(params: any) {
  return requestClient.download('/mes/prod-report/export-excel', { params });
}

/** 取消生产报工 */
export function cancelProdReport(
  data: MesProdReportApi.ProdReportCancelReq,
) {
  return requestClient.post('/mes/prod-report/cancel', data);
}
