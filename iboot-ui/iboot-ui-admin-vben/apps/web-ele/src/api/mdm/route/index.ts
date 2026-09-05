import type { Dayjs } from 'dayjs';

import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmRouteApi {
  export type Id = number | string;

  /** 工序用料信息 */
  export interface RouteOpMaterial {
    id: Id;
    routeOpId: Id;
    matId: Id;
    matCode?: string;
    matName?: string;
    matSpec?: string;
    qty: number;
    uomId: Id;
    uomCode?: string;
    uomName?: string;
    remark?: string;
  }

  /** 工艺路线工序信息 */
  export interface RouteOp {
    id: Id;
    routeId: Id;
    seq: number;
    opId: Id;
    opCode?: string;
    opName?: string;
    wcId: Id;
    wcCode?: string;
    wcName?: string;
    parallelGroup?: string;
    keyFlag: boolean;
    faiFlag: boolean;
    skipPolicy?: string;
    remark?: string;
    materialCount?: number;
    materials?: RouteOpMaterial[];
  }

  /** 工艺路线信息 */
  export interface Route {
    id: Id; // 主键
    matId?: Id; // 物料编号
    matCode?: string; // 物料编码
    matName?: string; // 物料名称
    version?: string; // 版本号
    status?: number; // 状态
    effectiveDate?: Dayjs | string; // 生效日期
    expiryDate: Dayjs | string; // 失效日期
    remark: string; // 备注
  }

  export interface RouteDetail extends Route {
    operations: RouteOp[];
  }

  /** 工艺路线复制新版本请求 */
  export interface RouteCopy {
    id: Id; // 原工艺路线编号
    version: string; // 新版本号
    effectiveDate?: string; // 生效日期，为空取当天
  }
}

/** 查询工艺路线分页 */
export function getRoutePage(params: PageParam) {
  return requestClient.get<PageResult<MdmRouteApi.Route>>('/mdm/route/page', {
    params,
  });
}

/** 查询工艺路线详情 */
export function getRoute(id: MdmRouteApi.Id) {
  return requestClient.get<MdmRouteApi.Route>(`/mdm/route/get?id=${id}`);
}

/** 查询工艺路线编排详情 */
export function getRouteDetail(id: MdmRouteApi.Id) {
  return requestClient.get<MdmRouteApi.RouteDetail>(
    `/mdm/route/detail?id=${id}`,
  );
}

/** 新增工艺路线 */
export function createRoute(data: MdmRouteApi.Route) {
  return requestClient.post('/mdm/route/create', data);
}

/** 修改工艺路线 */
export function updateRoute(data: MdmRouteApi.Route) {
  return requestClient.put('/mdm/route/update', data);
}

/** 删除工艺路线 */
export function deleteRoute(id: MdmRouteApi.Id) {
  return requestClient.delete(`/mdm/route/delete?id=${id}`);
}

/** 批量删除工艺路线 */
export function deleteRouteList(ids: MdmRouteApi.Id[]) {
  return requestClient.delete(`/mdm/route/delete-list?ids=${ids.join(',')}`);
}

/** 导出工艺路线 */
export function exportRoute(params: any) {
  return requestClient.download('/mdm/route/export-excel', { params });
}

/** 复制工艺路线为新版本 */
export function copyRoute(data: MdmRouteApi.RouteCopy) {
  return requestClient.post<number>('/mdm/route/copy', data);
}

// ==================== 子表（工艺路线工序） ====================

/** 获得工艺路线工序分页 */
export function getRouteOpPage(params: PageParam) {
  return requestClient.get<PageResult<MdmRouteApi.RouteOp>>(
    `/mdm/route/route-op/page`,
    { params },
  );
}
/** 新增工艺路线工序 */
export function createRouteOp(data: MdmRouteApi.RouteOp) {
  return requestClient.post(`/mdm/route/route-op/create`, data);
}

/** 修改工艺路线工序 */
export function updateRouteOp(data: MdmRouteApi.RouteOp) {
  return requestClient.put(`/mdm/route/route-op/update`, data);
}

/** 更新工艺路线工序排序 */
export function updateRouteOpSort(
  routeId: MdmRouteApi.Id,
  orderedIds: MdmRouteApi.Id[],
) {
  return requestClient.put(`/mdm/route/route-op/update-sort`, {
    orderedIds,
    routeId,
  });
}

/** 删除工艺路线工序 */
export function deleteRouteOp(id: MdmRouteApi.Id) {
  return requestClient.delete(`/mdm/route/route-op/delete?id=${id}`);
}

/** 批量删除工艺路线工序 */
export function deleteRouteOpList(ids: MdmRouteApi.Id[]) {
  return requestClient.delete(
    `/mdm/route/route-op/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得工艺路线工序 */
export function getRouteOp(id: MdmRouteApi.Id) {
  return requestClient.get<MdmRouteApi.RouteOp>(
    `/mdm/route/route-op/get?id=${id}`,
  );
}
