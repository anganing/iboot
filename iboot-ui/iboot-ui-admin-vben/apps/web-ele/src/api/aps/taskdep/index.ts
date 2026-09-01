import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsTaskDepApi {
  /** APS-排程任务依赖信息 */
  export interface TaskDep {
    id?: number; // 主键
    versionId?: number; // 排程版本编号，由前置任务反推
    versionNo?: number; // 排程版本号
    fromTaskId?: number; // 前置排程任务编号
    fromTaskNo?: string; // 前置任务编号
    fromOpCode?: string; // 前置工序编码
    fromOpName?: string; // 前置工序名称
    toTaskId?: number; // 后置排程任务编号
    toTaskNo?: string; // 后置任务编号
    toOpCode?: string; // 后置工序编码
    toOpName?: string; // 后置工序名称
    depType?: string; // 依赖类型，字典 aps_task_dep_type
    lagMinutes?: number; // 依赖间隔分钟数
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }
}

/** 查询排程任务依赖分页 */
export function getTaskDepPage(params: PageParam) {
  return requestClient.get<PageResult<ApsTaskDepApi.TaskDep>>(
    '/aps/task-dep/page',
    { params },
  );
}

/** 查询排程任务依赖详情 */
export function getTaskDep(id: number) {
  return requestClient.get<ApsTaskDepApi.TaskDep>(
    `/aps/task-dep/get?id=${id}`,
  );
}

/** 新增排程任务依赖 */
export function createTaskDep(data: ApsTaskDepApi.TaskDep) {
  return requestClient.post('/aps/task-dep/create', data);
}

/** 修改排程任务依赖 */
export function updateTaskDep(data: ApsTaskDepApi.TaskDep) {
  return requestClient.put('/aps/task-dep/update', data);
}

/** 删除排程任务依赖 */
export function deleteTaskDep(id: number) {
  return requestClient.delete(`/aps/task-dep/delete?id=${id}`);
}

/** 批量删除排程任务依赖 */
export function deleteTaskDepList(ids: number[]) {
  return requestClient.delete(`/aps/task-dep/delete-list?ids=${ids.join(',')}`);
}

/** 导出排程任务依赖 */
export function exportTaskDep(params: any) {
  return requestClient.download('/aps/task-dep/export-excel', { params });
}
