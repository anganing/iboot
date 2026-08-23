import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmRouteOpMatApi {
  /** 工序用料信息 */
  export interface RouteOpMat {
    id: number; // 主键
    routeOpId?: number; // 路线工序编号
    matId?: number; // 物料编号
    qty?: number; // 数量
    uomId?: number; // 计量单位编号
    remark: string; // 备注
  }
}

/** 查询工序用料分页 */
export function getRouteOpMatPage(params: PageParam) {
  return requestClient.get<PageResult<MdmRouteOpMatApi.RouteOpMat>>(
    '/mdm/route-op-mat/page',
    { params },
  );
}

/** 查询工序用料详情 */
export function getRouteOpMat(id: number) {
  return requestClient.get<MdmRouteOpMatApi.RouteOpMat>(
    `/mdm/route-op-mat/get?id=${id}`,
  );
}

/** 新增工序用料 */
export function createRouteOpMat(data: MdmRouteOpMatApi.RouteOpMat) {
  return requestClient.post('/mdm/route-op-mat/create', data);
}

/** 修改工序用料 */
export function updateRouteOpMat(data: MdmRouteOpMatApi.RouteOpMat) {
  return requestClient.put('/mdm/route-op-mat/update', data);
}

/** 删除工序用料 */
export function deleteRouteOpMat(id: number) {
  return requestClient.delete(`/mdm/route-op-mat/delete?id=${id}`);
}

/** 批量删除工序用料 */
export function deleteRouteOpMatList(ids: number[]) {
  return requestClient.delete(
    `/mdm/route-op-mat/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出工序用料 */
export function exportRouteOpMat(params: any) {
  return requestClient.download('/mdm/route-op-mat/export-excel', { params });
}


