import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesDispatchTaskApi {
  /** MES-派工任务信息 */
  export interface DispatchTask {
    id: number; // 主键
    dispatchNo?: string; // 派工单号
    opTaskId?: number; // 工序任务编号
    operatorId: number; // 作业人员编号
    teamId: number; // 作业班组编号
    resId?: number; // 生产资源编号
    shiftId: number; // 班次编号
    plannedStartTime: string | Dayjs; // 计划开始时间
    plannedEndTime: string | Dayjs; // 计划结束时间
    status?: string; // 状态：PENDING、ACCEPTED、RUNNING、DONE、CANCELLED
    remark: string; // 备注
  }
}

/** 查询MES-派工任务分页 */
export function getDispatchTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesDispatchTaskApi.DispatchTask>>(
    '/mes/dispatch-task/page',
    { params },
  );
}

/** 查询MES-派工任务详情 */
export function getDispatchTask(id: number) {
  return requestClient.get<MesDispatchTaskApi.DispatchTask>(
    `/mes/dispatch-task/get?id=${id}`,
  );
}

/** 新增MES-派工任务 */
export function createDispatchTask(data: MesDispatchTaskApi.DispatchTask) {
  return requestClient.post('/mes/dispatch-task/create', data);
}

/** 修改MES-派工任务 */
export function updateDispatchTask(data: MesDispatchTaskApi.DispatchTask) {
  return requestClient.put('/mes/dispatch-task/update', data);
}

/** 删除MES-派工任务 */
export function deleteDispatchTask(id: number) {
  return requestClient.delete(`/mes/dispatch-task/delete?id=${id}`);
}

/** 批量删除MES-派工任务 */
export function deleteDispatchTaskList(ids: number[]) {
  return requestClient.delete(
    `/mes/dispatch-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-派工任务 */
export function exportDispatchTask(params: any) {
  return requestClient.download('/mes/dispatch-task/export-excel', { params });
}

/** 下载MES-派工任务导入模板 */
export function importDispatchTaskTemplate() {
  return requestClient.download('/mes/dispatch-task/get-import-template');
}

/** 导入MES-派工任务 */
export function importDispatchTask(file: File) {
  return requestClient.upload('/mes/dispatch-task/import', { file });
}

