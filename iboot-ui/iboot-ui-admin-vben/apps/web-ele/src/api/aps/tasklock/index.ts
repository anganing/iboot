import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsTaskLockApi {
  /** APS-排程任务锁定记录信息 */
  export interface TaskLock {
    id: number; // 主键
    taskId?: number; // 排程任务编号
    lockType?: string; // 锁定类型：STARTED、MANUAL、MATERIAL、RESOURCE
    reason?: string; // 锁定原因
    lockedBy: number; // 锁定人员编号
    lockedTime?: string | Dayjs; // 锁定时间
    unlockTime: string | Dayjs; // 解锁时间
    status?: string; // 状态：LOCKED、UNLOCKED
    remark: string; // 备注
  }
}

/** 查询APS-排程任务锁定记录分页 */
export function getTaskLockPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskLockApi.TaskLock>>(
    '/aps/task-lock/page',
    { params },
  );
}

/** 查询APS-排程任务锁定记录详情 */
export function getTaskLock(id: number) {
  return requestClient.get<ApsTaskLockApi.TaskLock>(
    `/aps/task-lock/get?id=${id}`,
  );
}

/** 新增APS-排程任务锁定记录 */
export function createTaskLock(data: ApsTaskLockApi.TaskLock) {
  return requestClient.post('/aps/task-lock/create', data);
}

/** 修改APS-排程任务锁定记录 */
export function updateTaskLock(data: ApsTaskLockApi.TaskLock) {
  return requestClient.put('/aps/task-lock/update', data);
}

/** 删除APS-排程任务锁定记录 */
export function deleteTaskLock(id: number) {
  return requestClient.delete(`/aps/task-lock/delete?id=${id}`);
}

/** 批量删除APS-排程任务锁定记录 */
export function deleteTaskLockList(ids: number[]) {
  return requestClient.delete(
    `/aps/task-lock/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程任务锁定记录 */
export function exportTaskLock(params: any) {
  return requestClient.download('/aps/task-lock/export-excel', { params });
}

/** 下载APS-排程任务锁定记录导入模板 */
export function importTaskLockTemplate() {
  return requestClient.download('/aps/task-lock/get-import-template');
}

/** 导入APS-排程任务锁定记录 */
export function importTaskLock(file: File) {
  return requestClient.upload('/aps/task-lock/import', { file });
}

