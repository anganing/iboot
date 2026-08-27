import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmCustomerApi {
  /** 客户信息 */
  export interface Customer {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询客户分页 */
export function getCustomerPage(params: PageParam) {
  return requestClient.get<PageResult<MdmCustomerApi.Customer>>(
    '/mdm/customer/page',
    { params },
  );
}

/** 查询客户详情 */
export function getCustomer(id: number) {
  return requestClient.get<MdmCustomerApi.Customer>(
    `/mdm/customer/get?id=${id}`,
  );
}

/** 新增客户 */
export function createCustomer(data: MdmCustomerApi.Customer) {
  return requestClient.post('/mdm/customer/create', data);
}

/** 修改客户 */
export function updateCustomer(data: MdmCustomerApi.Customer) {
  return requestClient.put('/mdm/customer/update', data);
}

/** 删除客户 */
export function deleteCustomer(id: number) {
  return requestClient.delete(`/mdm/customer/delete?id=${id}`);
}

/** 批量删除客户 */
export function deleteCustomerList(ids: number[]) {
  return requestClient.delete(`/mdm/customer/delete-list?ids=${ids.join(',')}`);
}

/** 导出客户 */
export function exportCustomer(params: any) {
  return requestClient.download('/mdm/customer/export-excel', { params });
}
