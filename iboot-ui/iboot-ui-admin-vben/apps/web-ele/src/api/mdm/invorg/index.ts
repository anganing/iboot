import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmInvOrgApi {
  /** 库存组织信息。 */
  export interface InvOrg {
    id?: number;
    code: string;
    name: string;
    status: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getInvOrgPage(params: PageParam) {
  return requestClient.get<PageResult<MdmInvOrgApi.InvOrg>>(
    '/mdm/inv-org/page',
    { params },
  );
}

export function getInvOrg(id: number) {
  return requestClient.get<MdmInvOrgApi.InvOrg>(`/mdm/inv-org/get?id=${id}`);
}

export function createInvOrg(data: MdmInvOrgApi.InvOrg) {
  return requestClient.post('/mdm/inv-org/create', data);
}

export function updateInvOrg(data: MdmInvOrgApi.InvOrg) {
  return requestClient.put('/mdm/inv-org/update', data);
}

export function deleteInvOrg(id: number) {
  return requestClient.delete(`/mdm/inv-org/delete?id=${id}`);
}
