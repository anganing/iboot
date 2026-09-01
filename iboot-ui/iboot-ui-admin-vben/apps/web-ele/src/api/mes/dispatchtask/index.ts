import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesDispatchTaskApi {
  /**
   * MES-派工任务。
   *
   * 工序任务号与工序名由模块内 JOIN 带出；人员、班组、资源、班次名称由 Service 走门面补全。
   */
  export interface DispatchTask {
    id?: number;
    dispatchNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    opName?: string;
    workOrderNo?: string;
    operatorId?: number;
    operatorName?: string;
    teamId?: number;
    teamName?: string;
    resId?: number;
    resName?: string;
    shiftId?: number;
    shiftName?: string;
    plannedStartTime?: number | string;
    plannedEndTime?: number | string;
    status?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询派工任务分页 */
export function getDispatchTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesDispatchTaskApi.DispatchTask>>(
    '/mes/dispatch-task/page',
    { params },
  );
}

/** 查询派工任务详情 */
export function getDispatchTask(id: number) {
  return requestClient.get<MesDispatchTaskApi.DispatchTask>(
    `/mes/dispatch-task/get?id=${id}`,
  );
}

/** 新增派工任务 */
export function createDispatchTask(data: MesDispatchTaskApi.DispatchTask) {
  return requestClient.post('/mes/dispatch-task/create', data);
}

/** 修改派工任务 */
export function updateDispatchTask(data: MesDispatchTaskApi.DispatchTask) {
  return requestClient.put('/mes/dispatch-task/update', data);
}

/** 删除派工任务 */
export function deleteDispatchTask(id: number) {
  return requestClient.delete(`/mes/dispatch-task/delete?id=${id}`);
}

/** 批量删除派工任务 */
export function deleteDispatchTaskList(ids: number[]) {
  return requestClient.delete(
    `/mes/dispatch-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出派工任务 */
export function exportDispatchTask(params: any) {
  return requestClient.download('/mes/dispatch-task/export-excel', { params });
}
