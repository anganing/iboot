import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmSupplierApi {
  /** 供应商信息 */
  export interface Supplier {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询供应商分页 */
export function getSupplierPage(params: PageParam) {
  return requestClient.get<PageResult<MdmSupplierApi.Supplier>>(
    '/mdm/supplier/page',
    { params },
  );
}

/** 查询供应商详情 */
export function getSupplier(id: number) {
  return requestClient.get<MdmSupplierApi.Supplier>(
    `/mdm/supplier/get?id=${id}`,
  );
}

/** 新增供应商 */
export function createSupplier(data: MdmSupplierApi.Supplier) {
  return requestClient.post('/mdm/supplier/create', data);
}

/** 修改供应商 */
export function updateSupplier(data: MdmSupplierApi.Supplier) {
  return requestClient.put('/mdm/supplier/update', data);
}

/** 删除供应商 */
export function deleteSupplier(id: number) {
  return requestClient.delete(`/mdm/supplier/delete?id=${id}`);
}

/** 批量删除供应商 */
export function deleteSupplierList(ids: number[]) {
  return requestClient.delete(`/mdm/supplier/delete-list?ids=${ids.join(',')}`);
}

/** 导出供应商 */
export function exportSupplier(params: any) {
  return requestClient.download('/mdm/supplier/export-excel', { params });
}
