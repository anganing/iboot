import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesReworkApi {
  /**
   * MES-返工任务。
   *
   * 生产工单、作业工单编号与返工路线版本由后端从工序任务与工艺路线带出，前端只读。
   */
  export interface Rework {
    id?: number;
    reworkNo?: string;
    sourceQcTaskId?: number;
    sourceQcTaskNo?: string;
    prodOrderId?: number;
    prodOrderNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    opName?: string;
    qty?: number;
    routeId?: number;
    routeVersion?: string;
    status?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询返工任务分页 */
export function getReworkPage(params: PageParam) {
  return requestClient.get<PageResult<MesReworkApi.Rework>>(
    '/mes/rework/page',
    { params },
  );
}

/** 查询返工任务详情 */
export function getRework(id: number) {
  return requestClient.get<MesReworkApi.Rework>(`/mes/rework/get?id=${id}`);
}

/** 新增返工任务 */
export function createRework(data: MesReworkApi.Rework) {
  return requestClient.post('/mes/rework/create', data);
}

/** 修改返工任务 */
export function updateRework(data: MesReworkApi.Rework) {
  return requestClient.put('/mes/rework/update', data);
}

/** 删除返工任务 */
export function deleteRework(id: number) {
  return requestClient.delete(`/mes/rework/delete?id=${id}`);
}

/** 批量删除返工任务 */
export function deleteReworkList(ids: number[]) {
  return requestClient.delete(`/mes/rework/delete-list?ids=${ids.join(',')}`);
}

/** 导出返工任务 */
export function exportRework(params: any) {
  return requestClient.download('/mes/rework/export-excel', { params });
}
