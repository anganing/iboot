import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsTaskDepApi {
  /** APS-排程任务依赖信息 */
  export interface TaskDep {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    fromTaskId?: number; // 前置排程任务编号
    toTaskId?: number; // 后置排程任务编号
    depType?: string; // 依赖类型：FINISH_START、START_START、FINISH_FINISH
    lagMinutes?: number; // 依赖间隔分钟数
    remark: string; // 备注
  }
}

/** 查询APS-排程任务依赖分页 */
export function getTaskDepPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskDepApi.TaskDep>>(
    '/aps/task-dep/page',
    { params },
  );
}

/** 查询APS-排程任务依赖详情 */
export function getTaskDep(id: number) {
  return requestClient.get<ApsTaskDepApi.TaskDep>(
    `/aps/task-dep/get?id=${id}`,
  );
}

/** 新增APS-排程任务依赖 */
export function createTaskDep(data: ApsTaskDepApi.TaskDep) {
  return requestClient.post('/aps/task-dep/create', data);
}

/** 修改APS-排程任务依赖 */
export function updateTaskDep(data: ApsTaskDepApi.TaskDep) {
  return requestClient.put('/aps/task-dep/update', data);
}

/** 删除APS-排程任务依赖 */
export function deleteTaskDep(id: number) {
  return requestClient.delete(`/aps/task-dep/delete?id=${id}`);
}

/** 批量删除APS-排程任务依赖 */
export function deleteTaskDepList(ids: number[]) {
  return requestClient.delete(
    `/aps/task-dep/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程任务依赖 */
export function exportTaskDep(params: any) {
  return requestClient.download('/aps/task-dep/export-excel', { params });
}

/** 下载APS-排程任务依赖导入模板 */
export function importTaskDepTemplate() {
  return requestClient.download('/aps/task-dep/get-import-template');
}

/** 导入APS-排程任务依赖 */
export function importTaskDep(file: File) {
  return requestClient.upload('/aps/task-dep/import', { file });
}

