import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsLocApi {
  /** WMS-库位信息 */
  export interface Loc {
    id: number; // 主键
    whId?: number; // 仓库编号
    zoneId?: number; // 库区编号
    code?: string; // 库位编码
    locType?: string; // 库位类型：ASRS_CELL、FLAT_AREA、RACK_BIN、LINE_SIDE、BLACK_BOX
    aisleNo: string; // 巷道号
    rowNo: string; // 排号
    colNo: string; // 列号
    levelNo: string; // 层号
    depthNo: number; // 纵深号
    maxLength: number; // 容器最大长度，单位毫米
    maxWidth: number; // 容器最大宽度，单位毫米
    maxHeight: number; // 容器最大高度，单位毫米
    maxWeight: number; // 最大承重
    maxVolume: number; // 最大容积
    maxContainerQty: number; // 最大容器数量
    dimensionCheckMode?: string; // 尺寸校验模式：NONE、HEIGHT_ONLY、FULL_DIM
    status?: string; // 状态：EMPTY、OCCUPIED、LOCKED、DISABLED
    remark: string; // 备注
  }
}

/** 查询WMS-库位分页 */
export function getLocPage(params: PageParam) {
  return requestClient.get<PageResult<WmsLocApi.Loc>>(
    '/wms/loc/page',
    { params },
  );
}

/** 查询WMS-库位详情 */
export function getLoc(id: number) {
  return requestClient.get<WmsLocApi.Loc>(
    `/wms/loc/get?id=${id}`,
  );
}

/** 新增WMS-库位 */
export function createLoc(data: WmsLocApi.Loc) {
  return requestClient.post('/wms/loc/create', data);
}

/** 修改WMS-库位 */
export function updateLoc(data: WmsLocApi.Loc) {
  return requestClient.put('/wms/loc/update', data);
}

/** 删除WMS-库位 */
export function deleteLoc(id: number) {
  return requestClient.delete(`/wms/loc/delete?id=${id}`);
}

/** 批量删除WMS-库位 */
export function deleteLocList(ids: number[]) {
  return requestClient.delete(
    `/wms/loc/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-库位 */
export function exportLoc(params: any) {
  return requestClient.download('/wms/loc/export-excel', { params });
}

/** 下载WMS-库位导入模板 */
export function importLocTemplate() {
  return requestClient.download('/wms/loc/get-import-template');
}

/** 导入WMS-库位 */
export function importLoc(file: File) {
  return requestClient.upload('/wms/loc/import', { file });
}

