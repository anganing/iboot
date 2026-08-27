import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmRouteOpMatApi {
  export type Id = number | string;

  /** 工序用料信息 */
  export interface RouteOpMat {
    id: Id; // 主键
    routeOpId?: Id; // 路线工序编号
    matId?: Id; // 物料编号
    matCode?: string; // 物料编码
    matName?: string; // 物料名称
    matSpec?: string; // 规格型号
    qty?: number; // 数量
    uomId?: Id; // 计量单位编号
    uomCode?: string; // 计量单位编码
    uomName?: string; // 计量单位名称
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
export function getRouteOpMat(id: MdmRouteOpMatApi.Id) {
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
export function deleteRouteOpMat(id: MdmRouteOpMatApi.Id) {
  return requestClient.delete(`/mdm/route-op-mat/delete?id=${id}`);
}

/** 批量删除工序用料 */
export function deleteRouteOpMatList(ids: MdmRouteOpMatApi.Id[]) {
  return requestClient.delete(
    `/mdm/route-op-mat/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出工序用料 */
export function exportRouteOpMat(params: any) {
  return requestClient.download('/mdm/route-op-mat/export-excel', { params });
}
