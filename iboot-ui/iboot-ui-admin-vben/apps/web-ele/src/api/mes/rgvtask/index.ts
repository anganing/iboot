import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesRgvTaskApi {
  /**
   * MES-RGV 工位流转任务。
   *
   * 工位名称落在 mdm 的生产资源上，由 Service 走门面补全；完成时间由外部调度系统回报。
   */
  export interface RgvTask {
    id?: number;
    taskNo?: string;
    externalTaskNo?: string;
    taskType?: string;
    workOrderId?: number;
    workOrderNo?: string;
    opTaskId?: number;
    opTaskNo?: string;
    targetOpTaskId?: number;
    targetOpTaskNo?: string;
    fromStationId?: number;
    fromStationName?: string;
    toStationId?: number;
    toStationName?: string;
    carrierNo?: string;
    externalSystemCode?: string;
    dispatchStatus?: string;
    status?: string;
    idempotencyKey?: string;
    completedTime?: string;
    version?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询 RGV 流转任务分页 */
export function getRgvTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesRgvTaskApi.RgvTask>>(
    '/mes/rgv-task/page',
    { params },
  );
}

/** 查询 RGV 流转任务详情 */
export function getRgvTask(id: number) {
  return requestClient.get<MesRgvTaskApi.RgvTask>(`/mes/rgv-task/get?id=${id}`);
}

/** 新增 RGV 流转任务 */
export function createRgvTask(data: MesRgvTaskApi.RgvTask) {
  return requestClient.post('/mes/rgv-task/create', data);
}

/** 修改 RGV 流转任务 */
export function updateRgvTask(data: MesRgvTaskApi.RgvTask) {
  return requestClient.put('/mes/rgv-task/update', data);
}

/** 删除 RGV 流转任务 */
export function deleteRgvTask(id: number) {
  return requestClient.delete(`/mes/rgv-task/delete?id=${id}`);
}

/** 批量删除 RGV 流转任务 */
export function deleteRgvTaskList(ids: number[]) {
  return requestClient.delete(`/mes/rgv-task/delete-list?ids=${ids.join(',')}`);
}

/** 导出 RGV 流转任务 */
export function exportRgvTask(params: any) {
  return requestClient.download('/mes/rgv-task/export-excel', { params });
}
