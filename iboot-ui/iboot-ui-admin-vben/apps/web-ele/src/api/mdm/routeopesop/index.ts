import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmRouteOpEsopApi {
  /** ESOP 作业步骤附件 */
  export interface RouteOpEsopFile {
    id?: number; // 主键
    esopId?: number; // 作业步骤编号
    seq?: number; // 附件展示顺序
    fileType?: string; // 文件类型，由后端按扩展名判定
    fileName?: string; // 原始文件名
    fileUrl?: string; // 文件地址
    fileId?: number; // infra 文件编号
  }

  /** ESOP 作业步骤信息 */
  export interface RouteOpEsop {
    id: number; // 主键
    routeOpId?: number; // 路线工序编号
    seq?: number; // 展示顺序
    title?: string; // 标题
    content?: string; // 作业要点文本
    paramJson?: string; // 工艺参数 JSON
    files?: RouteOpEsopFile[]; // 附件列表
    fileCount?: number; // 附件数量
    requiredConfirmFlag?: boolean; // 是否要求确认阅读
    status?: number; // 状态
  }
}

/** 查询 ESOP 作业步骤分页 */
export function getRouteOpEsopPage(params: PageParam) {
  return requestClient.get<PageResult<MdmRouteOpEsopApi.RouteOpEsop>>(
    '/mdm/route-op-esop/page',
    { params },
  );
}

/** 查询 ESOP 作业步骤详情 */
export function getRouteOpEsop(id: number) {
  return requestClient.get<MdmRouteOpEsopApi.RouteOpEsop>(
    `/mdm/route-op-esop/get?id=${id}`,
  );
}

/** 新增 ESOP 作业步骤 */
export function createRouteOpEsop(data: MdmRouteOpEsopApi.RouteOpEsop) {
  return requestClient.post('/mdm/route-op-esop/create', data);
}

/** 修改 ESOP 作业步骤 */
export function updateRouteOpEsop(data: MdmRouteOpEsopApi.RouteOpEsop) {
  return requestClient.put('/mdm/route-op-esop/update', data);
}

/** 删除 ESOP 作业步骤 */
export function deleteRouteOpEsop(id: number) {
  return requestClient.delete(`/mdm/route-op-esop/delete?id=${id}`);
}

/** 批量删除 ESOP 作业步骤 */
export function deleteRouteOpEsopList(ids: number[]) {
  return requestClient.delete(
    `/mdm/route-op-esop/delete-list?ids=${ids.join(',')}`,
  );
}
