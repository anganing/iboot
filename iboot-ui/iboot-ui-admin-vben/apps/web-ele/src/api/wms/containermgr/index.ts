import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsContainerApi {
  /** WMS-容器主档信息 */
  export interface Container {
    id: number; // 主键
    containerNo?: string; // 容器编号或条码
    typeId?: number; // 容器类型编号
    invOrgId?: number; // 库存组织编号
    whId?: number; // 当前仓库编号
    locId?: number; // 当前库位编号，0表示未落位
    length: number; // 实际长度，单位毫米
    width: number; // 实际宽度，单位毫米
    height: number; // 实际高度，单位毫米
    grossWeight: number; // 实际毛重
    status?: string; // 状态：EMPTY、LOADED、IN_TRANSIT、STORED、CLOSED
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询WMS-容器主档分页 */
export function getContainerPage(params: PageParam) {
  return requestClient.get<PageResult<WmsContainerApi.Container>>(
    '/wms/container/page',
    { params },
  );
}

/** 查询WMS-容器主档详情 */
export function getContainer(id: number) {
  return requestClient.get<WmsContainerApi.Container>(
    `/wms/container/get?id=${id}`,
  );
}

/** 新增WMS-容器主档 */
export function createContainer(data: WmsContainerApi.Container) {
  return requestClient.post('/wms/container/create', data);
}

/** 修改WMS-容器主档 */
export function updateContainer(data: WmsContainerApi.Container) {
  return requestClient.put('/wms/container/update', data);
}

/** 删除WMS-容器主档 */
export function deleteContainer(id: number) {
  return requestClient.delete(`/wms/container/delete?id=${id}`);
}

/** 批量删除WMS-容器主档 */
export function deleteContainerList(ids: number[]) {
  return requestClient.delete(
    `/wms/container/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-容器主档 */
export function exportContainer(params: any) {
  return requestClient.download('/wms/container/export-excel', { params });
}

/** 下载WMS-容器主档导入模板 */
export function importContainerTemplate() {
  return requestClient.download('/wms/container/get-import-template');
}

/** 导入WMS-容器主档 */
export function importContainer(file: File) {
  return requestClient.upload('/wms/container/import', { file });
}

