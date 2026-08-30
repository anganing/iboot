import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsContainerApi {
  /** 容器信息 */
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

/** 查询容器分页 */
export function getContainerPage(params: PageParam) {
  return requestClient.get<PageResult<WmsContainerApi.Container>>(
    '/wms/container/page',
    { params },
  );
}

/** 查询容器详情 */
export function getContainer(id: number) {
  return requestClient.get<WmsContainerApi.Container>(
    `/wms/container/get?id=${id}`,
  );
}

/** 新增容器 */
export function createContainer(data: WmsContainerApi.Container) {
  return requestClient.post('/wms/container/create', data);
}

/** 修改容器 */
export function updateContainer(data: WmsContainerApi.Container) {
  return requestClient.put('/wms/container/update', data);
}

/** 删除容器 */
export function deleteContainer(id: number) {
  return requestClient.delete(`/wms/container/delete?id=${id}`);
}

/** 批量删除容器 */
export function deleteContainerList(ids: number[]) {
  return requestClient.delete(
    `/wms/container/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出容器 */
export function exportContainer(params: any) {
  return requestClient.download('/wms/container/export-excel', { params });
}

/** 下载容器导入模板 */
export function importContainerTemplate() {
  return requestClient.download('/wms/container/get-import-template');
}

/** 导入容器 */
export function importContainer(file: File) {
  return requestClient.upload('/wms/container/import', { file });
}

