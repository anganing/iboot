import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsTaskApi {
  /** APS-排程任务信息 */
  export interface Task {
    id?: number; // 主键
    versionId?: number; // 排程版本编号
    versionNo?: number; // 排程版本号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    taskNo?: string; // 排程任务编号
    prodOrderId?: number; // 生产工单编号
    prodOrderNo?: string; // 生产工单号快照
    workOrderId?: number; // 作业工单编号
    opTaskId?: number; // MES 工序任务编号
    routeOpId?: number; // 路线工序编号
    opCode?: string; // 工序编码快照
    opName?: string; // 工序名称快照
    plantId?: number; // 工厂编号
    plantName?: string; // 工厂名称
    workshopId?: number; // 车间编号
    workshopName?: string; // 车间名称
    projectId?: number; // 项目编号
    projectName?: string; // 项目名称
    projectTaskId?: number; // 项目任务编号
    projectTaskName?: string; // 项目任务名称
    resId?: number; // 排定生产资源编号
    resCode?: string; // 资源编码快照
    resName?: string; // 资源名称
    startTime?: string; // 计划开始时间
    endTime?: string; // 计划结束时间
    durationMinutes?: number; // 计划占用分钟数
    sequenceNo?: number; // 资源内执行顺序号
    lockedFlag?: boolean; // 是否锁定不可移动
    status?: string; // 状态，字典 aps_task_status
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 排程任务人工调整请求 */
  export interface TaskAdjust {
    id?: number; // 主键
    resId?: number; // 排定生产资源编号
    startTime?: string; // 计划开始时间
    endTime?: string; // 计划结束时间
    sequenceNo?: number; // 资源内执行顺序号
    remark?: string; // 备注
  }
}

/** 查询排程任务分页 */
export function getTaskPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskApi.Task>>('/aps/task/page', {
    params,
  });
}

/** 查询排程任务详情 */
export function getTask(id: number) {
  return requestClient.get<ApsTaskApi.Task>(`/aps/task/get?id=${id}`);
}

/** 调整排程任务的资源与计划时间 */
export function adjustTask(data: ApsTaskApi.TaskAdjust) {
  return requestClient.put('/aps/task/adjust', data);
}

/** 锁定排程任务，重排时不再移动它 */
export function lockTask(id: number, reason?: string) {
  return requestClient.put('/aps/task/lock', undefined, {
    params: { id, reason: reason ?? '' },
  });
}

/** 解锁排程任务 */
export function unlockTask(id: number, reason?: string) {
  return requestClient.put('/aps/task/unlock', undefined, {
    params: { id, reason: reason ?? '' },
  });
}

/** 导出排程任务 */
export function exportTask(params: any) {
  return requestClient.download('/aps/task/export-excel', { params });
}
