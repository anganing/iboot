import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesDispositionApi {
  /** MES-不合格品处置单信息 */
  export interface Disposition {
    id: number; // 主键
    dispositionNo?: string; // 不合格处置单号
    taskId?: number; // 质检任务编号
    dispositionType?: string; // 处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS
    qty?: number; // 处置数量
    targetBizType: string; // 联动目标业务类型
    targetBizNo: string; // 联动目标业务单号
    approvedBy: number; // 审批人员编号
    approveTime: string | Dayjs; // 审批时间
    status?: string; // 状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED
    idempotencyKey: string; // 处置幂等键
    remark: string; // 处置说明
  }
}

/** 查询MES-不合格品处置单分页 */
export function getDispositionPage(params: PageParam) {
  return requestClient.get<PageResult<MesDispositionApi.Disposition>>(
    '/mes/disposition/page',
    { params },
  );
}

/** 查询MES-不合格品处置单详情 */
export function getDisposition(id: number) {
  return requestClient.get<MesDispositionApi.Disposition>(
    `/mes/disposition/get?id=${id}`,
  );
}

/** 新增MES-不合格品处置单 */
export function createDisposition(data: MesDispositionApi.Disposition) {
  return requestClient.post('/mes/disposition/create', data);
}

/** 修改MES-不合格品处置单 */
export function updateDisposition(data: MesDispositionApi.Disposition) {
  return requestClient.put('/mes/disposition/update', data);
}

/** 删除MES-不合格品处置单 */
export function deleteDisposition(id: number) {
  return requestClient.delete(`/mes/disposition/delete?id=${id}`);
}

/** 批量删除MES-不合格品处置单 */
export function deleteDispositionList(ids: number[]) {
  return requestClient.delete(
    `/mes/disposition/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-不合格品处置单 */
export function exportDisposition(params: any) {
  return requestClient.download('/mes/disposition/export-excel', { params });
}

/** 下载MES-不合格品处置单导入模板 */
export function importDispositionTemplate() {
  return requestClient.download('/mes/disposition/get-import-template');
}

/** 导入MES-不合格品处置单 */
export function importDisposition(file: File) {
  return requestClient.upload('/mes/disposition/import', { file });
}

