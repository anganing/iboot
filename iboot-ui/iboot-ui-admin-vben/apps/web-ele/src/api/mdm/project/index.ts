import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmProjectApi {
  /** 项目信息 */
  export interface Project {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询项目分页 */
export function getProjectPage(params: PageParam) {
  return requestClient.get<PageResult<MdmProjectApi.Project>>(
    '/mdm/project/page',
    { params },
  );
}

/** 查询项目详情 */
export function getProject(id: number) {
  return requestClient.get<MdmProjectApi.Project>(`/mdm/project/get?id=${id}`);
}

/** 新增项目 */
export function createProject(data: MdmProjectApi.Project) {
  return requestClient.post('/mdm/project/create', data);
}

/** 修改项目 */
export function updateProject(data: MdmProjectApi.Project) {
  return requestClient.put('/mdm/project/update', data);
}

/** 删除项目 */
export function deleteProject(id: number) {
  return requestClient.delete(`/mdm/project/delete?id=${id}`);
}

/** 批量删除项目 */
export function deleteProjectList(ids: number[]) {
  return requestClient.delete(`/mdm/project/delete-list?ids=${ids.join(',')}`);
}

/** 导出项目 */
export function exportProject(params: any) {
  return requestClient.download('/mdm/project/export-excel', { params });
}
