import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsWhApi {
  /** 仓库信息 */
  export interface Wh {
    id: number; // 主键
    invOrgId?: number; // 库存组织编号
    plantId?: number; // 工厂编号
    code?: string; // 仓库编码
    name?: string; // 仓库名称
    whType?: string; // 仓库类型：ASRS-立库，FLAT-平库，LINE_SIDE-线边仓，VIRTUAL-虚拟仓，QC-质检仓，IN_TRANSIT-在途仓
    managedBy?: string; // 管理方式：MANUAL、WCS、AGV、MIXED
    externalSystemCode: string; // 外部设备系统编码
    status?: number; // 状态：0-启用，1-停用
    remark: string; // 备注
  }
}

/** 查询仓库分页 */
export function getWhPage(params: PageParam) {
  return requestClient.get<PageResult<WmsWhApi.Wh>>(
    '/wms/wh/page',
    { params },
  );
}

/** 查询仓库详情 */
export function getWh(id: number) {
  return requestClient.get<WmsWhApi.Wh>(
    `/wms/wh/get?id=${id}`,
  );
}

/** 新增仓库 */
export function createWh(data: WmsWhApi.Wh) {
  return requestClient.post('/wms/wh/create', data);
}

/** 修改仓库 */
export function updateWh(data: WmsWhApi.Wh) {
  return requestClient.put('/wms/wh/update', data);
}

/** 删除仓库 */
export function deleteWh(id: number) {
  return requestClient.delete(`/wms/wh/delete?id=${id}`);
}

/** 批量删除仓库 */
export function deleteWhList(ids: number[]) {
  return requestClient.delete(
    `/wms/wh/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出仓库 */
export function exportWh(params: any) {
  return requestClient.download('/wms/wh/export-excel', { params });
}

/** 下载仓库导入模板 */
export function importWhTemplate() {
  return requestClient.download('/wms/wh/get-import-template');
}

/** 导入仓库 */
export function importWh(file: File) {
  return requestClient.upload('/wms/wh/import', { file });
}

