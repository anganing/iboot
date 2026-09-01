import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsTaskLockApi {
  /** APS-排程任务锁定记录信息 */
  export interface TaskLock {
    id?: number; // 主键
    taskId?: number; // 排程任务编号
    taskNo?: string; // 排程任务号
    opCode?: string; // 工序编码
    opName?: string; // 工序名称
    resCode?: string; // 资源编码
    lockType?: string; // 锁定类型，字典 aps_task_lock_type
    reason?: string; // 锁定原因
    lockedBy?: number; // 锁定人员编号
    lockedByName?: string; // 锁定人
    lockedTime?: string; // 锁定时间
    unlockTime?: string; // 解锁时间
    status?: string; // 状态，字典 aps_task_lock_status
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }
}

/** 查询排程任务锁定记录分页 */
export function getTaskLockPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskLockApi.TaskLock>>(
    '/aps/task-lock/page',
    { params },
  );
}

/** 查询排程任务锁定记录详情 */
export function getTaskLock(id: number) {
  return requestClient.get<ApsTaskLockApi.TaskLock>(
    `/aps/task-lock/get?id=${id}`,
  );
}

/** 导出排程任务锁定记录 */
export function exportTaskLock(params: any) {
  return requestClient.download('/aps/task-lock/export-excel', { params });
}
