import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmUomApi {
  export interface Uom {
    id?: number;
    code: string;
    name: string;
    status: number;
    remark?: string;
    createTime?: Date;
  }
}

export function getUomPage(params: PageParam) {
  return requestClient.get<PageResult<MdmUomApi.Uom>>('/mdm/uom/page', {
    params,
  });
}

export function getUom(id: number) {
  return requestClient.get<MdmUomApi.Uom>(`/mdm/uom/get?id=${id}`);
}

export function createUom(data: MdmUomApi.Uom) {
  return requestClient.post('/mdm/uom/create', data);
}

export function updateUom(data: MdmUomApi.Uom) {
  return requestClient.put('/mdm/uom/update', data);
}

export function deleteUom(id: number) {
  return requestClient.delete(`/mdm/uom/delete?id=${id}`);
}
