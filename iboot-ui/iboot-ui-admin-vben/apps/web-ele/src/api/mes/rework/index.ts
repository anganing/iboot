import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesReworkApi {
  /** MES-返工任务信息 */
  export interface Rework {
    id: number; // 主键
    reworkNo?: string; // 返工任务编号
    sourceQcTaskId?: number; // 来源质检任务编号
    prodOrderId?: number; // 生产工单编号
    workOrderId: number; // 作业工单编号
    opTaskId?: number; // 返工起始工序任务编号
    qty?: number; // 返工数量
    routeId: number; // 返工工艺路线编号
    routeVersion: string; // 返工工艺路线版本
    status?: string; // 状态：DRAFT、RELEASED、RUNNING、QC_PENDING、DONE、CANCELLED
    remark: string; // 备注
  }
}

/** 查询MES-返工任务分页 */
export function getReworkPage(params: PageParam) {
  return requestClient.get<PageResult<MesReworkApi.Rework>>(
    '/mes/rework/page',
    { params },
  );
}

/** 查询MES-返工任务详情 */
export function getRework(id: number) {
  return requestClient.get<MesReworkApi.Rework>(
    `/mes/rework/get?id=${id}`,
  );
}

/** 新增MES-返工任务 */
export function createRework(data: MesReworkApi.Rework) {
  return requestClient.post('/mes/rework/create', data);
}

/** 修改MES-返工任务 */
export function updateRework(data: MesReworkApi.Rework) {
  return requestClient.put('/mes/rework/update', data);
}

/** 删除MES-返工任务 */
export function deleteRework(id: number) {
  return requestClient.delete(`/mes/rework/delete?id=${id}`);
}

/** 批量删除MES-返工任务 */
export function deleteReworkList(ids: number[]) {
  return requestClient.delete(
    `/mes/rework/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-返工任务 */
export function exportRework(params: any) {
  return requestClient.download('/mes/rework/export-excel', { params });
}

/** 下载MES-返工任务导入模板 */
export function importReworkTemplate() {
  return requestClient.download('/mes/rework/get-import-template');
}

/** 导入MES-返工任务 */
export function importRework(file: File) {
  return requestClient.upload('/mes/rework/import', { file });
}

