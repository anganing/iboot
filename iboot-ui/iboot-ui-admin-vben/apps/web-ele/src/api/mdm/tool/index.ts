import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmToolApi {
  /** 工装模具刀具信息 */
  export interface Tool {
    id: number; // 主键
    code?: string; // 工装编码
    name?: string; // 工装名称
    toolType?: string; // 工装类型
    wcId?: number; // 所属工作中心编号
    wcCode?: string; // 工作中心编码
    wcName?: string; // 工作中心名称
    qtyTotal?: number; // 保有数量
    lifeType?: string; // 寿命类型
    lifeLimit?: number; // 寿命上限
    status?: number; // 状态
    remark?: string; // 备注
    createTime?: Date; // 创建时间
  }

  /** 工序工装信息 */
  export interface RouteOpTool {
    id: number; // 主键
    routeOpId?: number; // 路线工序编号
    toolId?: number; // 工装编号
    toolCode?: string; // 工装编码
    toolName?: string; // 工装名称
    qty?: number; // 需求数量
    requiredFlag?: boolean; // 是否必备
    createTime?: Date; // 创建时间
  }
}

/** 查询工装模具刀具分页 */
export function getToolPage(params: PageParam) {
  return requestClient.get<PageResult<MdmToolApi.Tool>>('/mdm/tool/page', {
    params,
  });
}

/** 查询工装模具刀具详情 */
export function getTool(id: number) {
  return requestClient.get<MdmToolApi.Tool>(`/mdm/tool/get?id=${id}`);
}

/** 新增工装模具刀具 */
export function createTool(data: MdmToolApi.Tool) {
  return requestClient.post('/mdm/tool/create', data);
}

/** 修改工装模具刀具 */
export function updateTool(data: MdmToolApi.Tool) {
  return requestClient.put('/mdm/tool/update', data);
}

/** 删除工装模具刀具 */
export function deleteTool(id: number) {
  return requestClient.delete(`/mdm/tool/delete?id=${id}`);
}

/** 批量删除工装模具刀具 */
export function deleteToolList(ids: number[]) {
  return requestClient.delete(`/mdm/tool/delete-list?ids=${ids.join(',')}`);
}

/** 导出工装模具刀具 */
export function exportTool(params: any) {
  return requestClient.download('/mdm/tool/export-excel', { params });
}

/** 查询工序工装分页 */
export function getRouteOpToolPage(
  params: PageParam & { routeOpId: number },
) {
  return requestClient.get<PageResult<MdmToolApi.RouteOpTool>>(
    '/mdm/tool/route-op/page',
    { params },
  );
}

/** 新增工序工装 */
export function createRouteOpTool(data: MdmToolApi.RouteOpTool) {
  return requestClient.post('/mdm/tool/route-op/create', data);
}

/** 修改工序工装 */
export function updateRouteOpTool(data: MdmToolApi.RouteOpTool) {
  return requestClient.put('/mdm/tool/route-op/update', data);
}

/** 查询工序工装详情 */
export function getRouteOpTool(id: number) {
  return requestClient.get<MdmToolApi.RouteOpTool>(
    `/mdm/tool/route-op/get?id=${id}`,
  );
}

/** 删除工序工装 */
export function deleteRouteOpTool(id: number) {
  return requestClient.delete(`/mdm/tool/route-op/delete?id=${id}`);
}

/** 批量删除工序工装 */
export function deleteRouteOpToolList(ids: number[]) {
  return requestClient.delete(
    `/mdm/tool/route-op/delete-list?ids=${ids.join(',')}`,
  );
}
