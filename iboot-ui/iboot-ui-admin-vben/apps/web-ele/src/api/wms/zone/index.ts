import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsZoneApi {
  /** WMS-库区信息 */
  export interface Zone {
    id: number; // 主键
    whId?: number; // 仓库编号
    code?: string; // 库区编码
    name?: string; // 库区名称
    zoneType?: string; // 库区类型：RAW、WIP、FG、QC、RETURN、SCRAP、RECEIVING、SHIPPING
    allowMixedMat?: boolean; // 是否允许混放物料
    allowMixedLot?: boolean; // 是否允许混放批次
    tempRange: string; // 温区要求
    status?: number; // 状态：0-启用，1-停用
    remark: string; // 备注
  }
}

/** 查询WMS-库区分页 */
export function getZonePage(params: PageParam) {
  return requestClient.get<PageResult<WmsZoneApi.Zone>>(
    '/wms/zone/page',
    { params },
  );
}

/** 查询WMS-库区详情 */
export function getZone(id: number) {
  return requestClient.get<WmsZoneApi.Zone>(
    `/wms/zone/get?id=${id}`,
  );
}

/** 新增WMS-库区 */
export function createZone(data: WmsZoneApi.Zone) {
  return requestClient.post('/wms/zone/create', data);
}

/** 修改WMS-库区 */
export function updateZone(data: WmsZoneApi.Zone) {
  return requestClient.put('/wms/zone/update', data);
}

/** 删除WMS-库区 */
export function deleteZone(id: number) {
  return requestClient.delete(`/wms/zone/delete?id=${id}`);
}

/** 批量删除WMS-库区 */
export function deleteZoneList(ids: number[]) {
  return requestClient.delete(
    `/wms/zone/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-库区 */
export function exportZone(params: any) {
  return requestClient.download('/wms/zone/export-excel', { params });
}

/** 下载WMS-库区导入模板 */
export function importZoneTemplate() {
  return requestClient.download('/wms/zone/get-import-template');
}

/** 导入WMS-库区 */
export function importZone(file: File) {
  return requestClient.upload('/wms/zone/import', { file });
}

