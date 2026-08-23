import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmProjectTaskApi {
  /** 项目任务信息 */
  export interface ProjectTask {
    id: number; // 主键
    projectId?: number; // 项目编号
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询项目任务分页 */
export function getProjectTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MdmProjectTaskApi.ProjectTask>>(
    '/mdm/project-task/page',
    { params },
  );
}

/** 查询项目任务详情 */
export function getProjectTask(id: number) {
  return requestClient.get<MdmProjectTaskApi.ProjectTask>(
    `/mdm/project-task/get?id=${id}`,
  );
}

/** 新增项目任务 */
export function createProjectTask(data: MdmProjectTaskApi.ProjectTask) {
  return requestClient.post('/mdm/project-task/create', data);
}

/** 修改项目任务 */
export function updateProjectTask(data: MdmProjectTaskApi.ProjectTask) {
  return requestClient.put('/mdm/project-task/update', data);
}

/** 删除项目任务 */
export function deleteProjectTask(id: number) {
  return requestClient.delete(`/mdm/project-task/delete?id=${id}`);
}

/** 批量删除项目任务 */
export function deleteProjectTaskList(ids: number[]) {
  return requestClient.delete(
    `/mdm/project-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出项目任务 */
export function exportProjectTask(params: any) {
  return requestClient.download('/mdm/project-task/export-excel', { params });
}


