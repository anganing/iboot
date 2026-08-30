import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace WmsContainerTypeApi {
  /** WMS-容器类型信息 */
  export interface ContainerType {
    id: number; // 主键
    code?: string; // 容器类型编码
    name?: string; // 容器类型名称
    stdLength: number; // 标准长度，单位毫米
    stdWidth: number; // 标准宽度，单位毫米
    stdHeight: number; // 标准高度，单位毫米
    maxWeight: number; // 最大承重
    maxVolume: number; // 最大容积
    status?: number; // 状态：0-启用，1-停用
    remark: string; // 备注
  }
}

/** 查询WMS-容器类型分页 */
export function getContainerTypePage(params: PageParam) {
  return requestClient.get<PageResult<WmsContainerTypeApi.ContainerType>>(
    '/wms/container-type/page',
    { params },
  );
}

/** 查询WMS-容器类型详情 */
export function getContainerType(id: number) {
  return requestClient.get<WmsContainerTypeApi.ContainerType>(
    `/wms/container-type/get?id=${id}`,
  );
}

/** 新增WMS-容器类型 */
export function createContainerType(data: WmsContainerTypeApi.ContainerType) {
  return requestClient.post('/wms/container-type/create', data);
}

/** 修改WMS-容器类型 */
export function updateContainerType(data: WmsContainerTypeApi.ContainerType) {
  return requestClient.put('/wms/container-type/update', data);
}

/** 删除WMS-容器类型 */
export function deleteContainerType(id: number) {
  return requestClient.delete(`/wms/container-type/delete?id=${id}`);
}

/** 批量删除WMS-容器类型 */
export function deleteContainerTypeList(ids: number[]) {
  return requestClient.delete(
    `/wms/container-type/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出WMS-容器类型 */
export function exportContainerType(params: any) {
  return requestClient.download('/wms/container-type/export-excel', { params });
}

/** 下载WMS-容器类型导入模板 */
export function importContainerTypeTemplate() {
  return requestClient.download('/wms/container-type/get-import-template');
}

/** 导入WMS-容器类型 */
export function importContainerType(file: File) {
  return requestClient.upload('/wms/container-type/import', { file });
}

