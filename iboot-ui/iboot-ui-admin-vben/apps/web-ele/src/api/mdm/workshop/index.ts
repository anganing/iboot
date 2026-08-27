import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmWorkshopApi {
  /** 车间信息 */
  export interface Workshop {
    id: number; // 主键
    plantId?: number; // 工厂编号
    plantCode?: string; // 工厂编码
    plantName?: string; // 工厂名称
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询车间分页 */
export function getWorkshopPage(params: PageParam) {
  return requestClient.get<PageResult<MdmWorkshopApi.Workshop>>(
    '/mdm/workshop/page',
    { params },
  );
}

/** 查询车间详情 */
export function getWorkshop(id: number) {
  return requestClient.get<MdmWorkshopApi.Workshop>(
    `/mdm/workshop/get?id=${id}`,
  );
}

/** 新增车间 */
export function createWorkshop(data: MdmWorkshopApi.Workshop) {
  return requestClient.post('/mdm/workshop/create', data);
}

/** 修改车间 */
export function updateWorkshop(data: MdmWorkshopApi.Workshop) {
  return requestClient.put('/mdm/workshop/update', data);
}

/** 删除车间 */
export function deleteWorkshop(id: number) {
  return requestClient.delete(`/mdm/workshop/delete?id=${id}`);
}

/** 批量删除车间 */
export function deleteWorkshopList(ids: number[]) {
  return requestClient.delete(`/mdm/workshop/delete-list?ids=${ids.join(',')}`);
}

/** 导出车间 */
export function exportWorkshop(params: any) {
  return requestClient.download('/mdm/workshop/export-excel', { params });
}
