import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesProdReportApi {
  /** MES-生产报工记录信息 */
  export interface ProdReport {
    id: number; // 主键
    reportNo?: string; // 报工单号
    prodOrderId?: number; // 生产工单编号
    workOrderId?: number; // 作业工单编号
    opTaskId?: number; // 工序任务编号
    workerId: number; // 报工作业人员编号
    resId?: number; // 生产资源编号
    shiftId: number; // 班次编号
    qtyGood?: number; // 合格数量
    qtyScrap?: number; // 报废数量
    qtyRework?: number; // 返工数量
    startTime?: string | Dayjs; // 本次作业开始时间
    endTime?: string | Dayjs; // 本次作业结束时间
    reportTime?: string | Dayjs; // 报工时间
    status?: string; // 状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED
    idempotencyKey: string; // 报工幂等键
    remark: string; // 备注
  }
}

/** 查询MES-生产报工记录分页 */
export function getProdReportPage(params: PageParam) {
  return requestClient.get<PageResult<MesProdReportApi.ProdReport>>(
    '/mes/prod-report/page',
    { params },
  );
}

/** 查询MES-生产报工记录详情 */
export function getProdReport(id: number) {
  return requestClient.get<MesProdReportApi.ProdReport>(
    `/mes/prod-report/get?id=${id}`,
  );
}

/** 新增MES-生产报工记录 */
export function createProdReport(data: MesProdReportApi.ProdReport) {
  return requestClient.post('/mes/prod-report/create', data);
}

/** 修改MES-生产报工记录 */
export function updateProdReport(data: MesProdReportApi.ProdReport) {
  return requestClient.put('/mes/prod-report/update', data);
}

/** 删除MES-生产报工记录 */
export function deleteProdReport(id: number) {
  return requestClient.delete(`/mes/prod-report/delete?id=${id}`);
}

/** 批量删除MES-生产报工记录 */
export function deleteProdReportList(ids: number[]) {
  return requestClient.delete(
    `/mes/prod-report/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-生产报工记录 */
export function exportProdReport(params: any) {
  return requestClient.download('/mes/prod-report/export-excel', { params });
}

/** 下载MES-生产报工记录导入模板 */
export function importProdReportTemplate() {
  return requestClient.download('/mes/prod-report/get-import-template');
}

/** 导入MES-生产报工记录 */
export function importProdReport(file: File) {
  return requestClient.upload('/mes/prod-report/import', { file });
}

