import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmResApi {
  /** 生产资源信息 */
  export interface Res {
    id: number; // 主键
    wcId?: number; // 工作中心编号
    resType?: string; // 资源类型
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询生产资源分页 */
export function getResPage(params: PageParam) {
  return requestClient.get<PageResult<MdmResApi.Res>>(
    '/mdm/res/page',
    { params },
  );
}

/** 查询生产资源详情 */
export function getRes(id: number) {
  return requestClient.get<MdmResApi.Res>(
    `/mdm/res/get?id=${id}`,
  );
}

/** 新增生产资源 */
export function createRes(data: MdmResApi.Res) {
  return requestClient.post('/mdm/res/create', data);
}

/** 修改生产资源 */
export function updateRes(data: MdmResApi.Res) {
  return requestClient.put('/mdm/res/update', data);
}

/** 删除生产资源 */
export function deleteRes(id: number) {
  return requestClient.delete(`/mdm/res/delete?id=${id}`);
}

/** 批量删除生产资源 */
export function deleteResList(ids: number[]) {
  return requestClient.delete(
    `/mdm/res/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出生产资源 */
export function exportRes(params: any) {
  return requestClient.download('/mdm/res/export-excel', { params });
}


