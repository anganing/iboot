import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmMatAltApi {
  /** 替代料信息 */
  export interface MatAlt {
    id: number; // 主键
    matId?: number; // 物料编号
    altMatId?: number; // 替代物料编号
    priority?: number; // 优先级
    remark: string; // 备注
  }
}

/** 查询替代料分页 */
export function getMatAltPage(params: PageParam) {
  return requestClient.get<PageResult<MdmMatAltApi.MatAlt>>(
    '/mdm/mat-alt/page',
    { params },
  );
}

/** 查询替代料详情 */
export function getMatAlt(id: number) {
  return requestClient.get<MdmMatAltApi.MatAlt>(
    `/mdm/mat-alt/get?id=${id}`,
  );
}

/** 新增替代料 */
export function createMatAlt(data: MdmMatAltApi.MatAlt) {
  return requestClient.post('/mdm/mat-alt/create', data);
}

/** 修改替代料 */
export function updateMatAlt(data: MdmMatAltApi.MatAlt) {
  return requestClient.put('/mdm/mat-alt/update', data);
}

/** 删除替代料 */
export function deleteMatAlt(id: number) {
  return requestClient.delete(`/mdm/mat-alt/delete?id=${id}`);
}

/** 批量删除替代料 */
export function deleteMatAltList(ids: number[]) {
  return requestClient.delete(
    `/mdm/mat-alt/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出替代料 */
export function exportMatAlt(params: any) {
  return requestClient.download('/mdm/mat-alt/export-excel', { params });
}


