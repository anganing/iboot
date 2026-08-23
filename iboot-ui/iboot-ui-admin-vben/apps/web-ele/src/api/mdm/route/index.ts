import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MdmRouteApi {
  /** 工艺路线工序信息 */
  export interface RouteOp {
            id: number; // 主键
            routeId?: number; // 工艺路线编号
            seq?: number; // 工序顺序
            opId?: number; // 标准工序编号
            wcId?: number; // 工作中心编号
            parallelGroup: string; // 并行组
            keyFlag?: boolean; // 是否关键工序
            faiFlag?: boolean; // 是否首件检验
            remark: string; // 备注
  }

  /** 工艺路线信息 */
  export interface Route {
    id: number; // 主键
    matId?: number; // 物料编号
    version?: string; // 版本号
    status?: string; // 状态
    effectiveDate?: string | Dayjs; // 生效日期
    expiryDate: string | Dayjs; // 失效日期
    remark: string; // 备注
  }
}

/** 查询工艺路线分页 */
export function getRoutePage(params: PageParam) {
  return requestClient.get<PageResult<MdmRouteApi.Route>>(
    '/mdm/route/page',
    { params },
  );
}

/** 查询工艺路线详情 */
export function getRoute(id: number) {
  return requestClient.get<MdmRouteApi.Route>(
    `/mdm/route/get?id=${id}`,
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
export function deleteRoute(id: number) {
  return requestClient.delete(`/mdm/route/delete?id=${id}`);
}

/** 批量删除工艺路线 */
export function deleteRouteList(ids: number[]) {
  return requestClient.delete(
    `/mdm/route/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出工艺路线 */
export function exportRoute(params: any) {
  return requestClient.download('/mdm/route/export-excel', { params });
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

/** 删除工艺路线工序 */
export function deleteRouteOp(id: number) {
  return requestClient.delete(`/mdm/route/route-op/delete?id=${id}`);
}

/** 批量删除工艺路线工序 */
export function deleteRouteOpList(ids: number[]) {
  return requestClient.delete(
    `/mdm/route/route-op/delete-list?ids=${ids.join(',')}`,
  );
}

/** 获得工艺路线工序 */
export function getRouteOp(id: number) {
  return requestClient.get<MdmRouteApi.RouteOp>(
    `/mdm/route/route-op/get?id=${id}`,
  );
}
