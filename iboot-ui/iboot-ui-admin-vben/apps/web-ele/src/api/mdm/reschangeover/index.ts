import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmResChangeoverApi {
  /** 换型矩阵信息 */
  export interface ResChangeover {
    id: number; // 主键
    resId?: number; // 资源编号，0-按资源组生效
    resCode?: string; // 资源编码
    resName?: string; // 资源名称
    resGroup?: string; // 资源组
    fromGroup?: string; // 换出换型分组
    toGroup?: string; // 换入换型分组
    changeoverMinutes?: number; // 换型分钟数
    status?: number; // 状态
    remark?: string; // 备注
  }
}

/** 查询换型矩阵分页 */
export function getResChangeoverPage(params: PageParam) {
  return requestClient.get<PageResult<MdmResChangeoverApi.ResChangeover>>(
    '/mdm/res-changeover/page',
    { params },
  );
}

/** 查询换型矩阵详情 */
export function getResChangeover(id: number) {
  return requestClient.get<MdmResChangeoverApi.ResChangeover>(
    `/mdm/res-changeover/get?id=${id}`,
  );
}

/** 新增换型矩阵 */
export function createResChangeover(data: MdmResChangeoverApi.ResChangeover) {
  return requestClient.post('/mdm/res-changeover/create', data);
}

/** 修改换型矩阵 */
export function updateResChangeover(data: MdmResChangeoverApi.ResChangeover) {
  return requestClient.put('/mdm/res-changeover/update', data);
}

/** 删除换型矩阵 */
export function deleteResChangeover(id: number) {
  return requestClient.delete(`/mdm/res-changeover/delete?id=${id}`);
}

/** 批量删除换型矩阵 */
export function deleteResChangeoverList(ids: number[]) {
  return requestClient.delete(
    `/mdm/res-changeover/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出换型矩阵 */
export function exportResChangeover(params: any) {
  return requestClient.download('/mdm/res-changeover/export-excel', { params });
}
