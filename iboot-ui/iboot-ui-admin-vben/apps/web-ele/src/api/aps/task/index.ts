import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsTaskApi {
  /** APS-工序排程任务信息 */
  export interface Task {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    taskNo?: string; // 排程任务编号
    prodOrderId?: number; // 生产工单编号
    prodOrderNo?: string; // 生产工单号快照
    workOrderId?: number; // 作业工单编号
    opTaskId?: number; // MES工序任务编号
    routeOpId?: number; // 路线工序编号
    opCode?: string; // 工序编码快照
    opName?: string; // 工序名称快照
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    resId?: number; // 排定生产资源编号
    resCode?: string; // 资源编码快照
    startTime?: string | Dayjs; // 计划开始时间
    endTime?: string | Dayjs; // 计划结束时间
    durationMinutes?: number; // 计划占用分钟数
    sequenceNo?: number; // 资源内执行顺序号
    lockedFlag?: boolean; // 是否锁定不可移动
    status?: string; // 状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED
    remark: string; // 备注
  }
}

/** 查询APS-工序排程任务分页 */
export function getTaskPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskApi.Task>>(
    '/aps/task/page',
    { params },
  );
}

/** 查询APS-工序排程任务详情 */
export function getTask(id: number) {
  return requestClient.get<ApsTaskApi.Task>(
    `/aps/task/get?id=${id}`,
  );
}

/** 新增APS-工序排程任务 */
export function createTask(data: ApsTaskApi.Task) {
  return requestClient.post('/aps/task/create', data);
}

/** 修改APS-工序排程任务 */
export function updateTask(data: ApsTaskApi.Task) {
  return requestClient.put('/aps/task/update', data);
}

/** 删除APS-工序排程任务 */
export function deleteTask(id: number) {
  return requestClient.delete(`/aps/task/delete?id=${id}`);
}

/** 批量删除APS-工序排程任务 */
export function deleteTaskList(ids: number[]) {
  return requestClient.delete(
    `/aps/task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-工序排程任务 */
export function exportTask(params: any) {
  return requestClient.download('/aps/task/export-excel', { params });
}

/** 下载APS-工序排程任务导入模板 */
export function importTaskTemplate() {
  return requestClient.download('/aps/task/get-import-template');
}

/** 导入APS-工序排程任务 */
export function importTask(file: File) {
  return requestClient.upload('/aps/task/import', { file });
}

