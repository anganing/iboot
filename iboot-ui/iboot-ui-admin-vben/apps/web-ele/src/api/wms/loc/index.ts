import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsLocApi {
  /** 库位信息 */
  export interface Loc {
    id: number; // 主键
    whId?: number; // 仓库编号
    zoneId?: number; // 库区编号
    code?: string; // 库位编码
    locType?: string; // 库位类型
    aisleNo?: string; // 巷道号
    rowNo?: string; // 排号
    colNo?: string; // 列号
    levelNo?: string; // 层号
    depthNo?: number; // 纵深号
    maxLength?: number; // 容器最大长度，单位毫米
    maxWidth?: number; // 容器最大宽度，单位毫米
    maxHeight?: number; // 容器最大高度，单位毫米
    maxWeight?: number; // 最大承重
    maxVolume?: number; // 最大容积
    maxContainerQty?: number; // 最大容器数量
    dimensionCheckMode?: string; // 尺寸校验模式
    status?: string; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 仓库编码
    whName?: string; // 仓库名称
    zoneCode?: string; // 库区编码
    zoneName?: string; // 库区名称
  }
}

/** 查询库位分页 */
export function getLocPage(params: PageParam) {
  return requestClient.get<PageResult<WmsLocApi.Loc>>(
    '/wms/loc/page',
    { params },
  );
}

/** 查询库位详情 */
export function getLoc(id: number) {
  return requestClient.get<WmsLocApi.Loc>(
    `/wms/loc/get?id=${id}`,
  );
}

/** 新增库位 */
export function createLoc(data: WmsLocApi.Loc) {
  return requestClient.post('/wms/loc/create', data);
}

/** 修改库位 */
export function updateLoc(data: WmsLocApi.Loc) {
  return requestClient.put('/wms/loc/update', data);
}

/** 删除库位 */
export function deleteLoc(id: number) {
  return requestClient.delete(`/wms/loc/delete?id=${id}`);
}

/** 批量删除库位 */
export function deleteLocList(ids: number[]) {
  return requestClient.delete(
    `/wms/loc/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出库位 */
export function exportLoc(params: any) {
  return requestClient.download('/wms/loc/export-excel', { params });
}

/** 下载库位导入模板 */
export function importLocTemplate() {
  return requestClient.download('/wms/loc/get-import-template');
}

/** 导入库位 */
export function importLoc(file: File) {
  return requestClient.upload('/wms/loc/import', { file });
}


