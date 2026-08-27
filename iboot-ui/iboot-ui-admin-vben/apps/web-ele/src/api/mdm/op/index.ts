import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmOpApi {
  /** 标准工序信息 */
  export interface Op {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    stdDuration?: number; // 标准时长（分钟）
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询标准工序分页 */
export function getOpPage(params: PageParam) {
  return requestClient.get<PageResult<MdmOpApi.Op>>('/mdm/op/page', { params });
}

/** 查询标准工序详情 */
export function getOp(id: number) {
  return requestClient.get<MdmOpApi.Op>(`/mdm/op/get?id=${id}`);
}

/** 新增标准工序 */
export function createOp(data: MdmOpApi.Op) {
  return requestClient.post('/mdm/op/create', data);
}

/** 修改标准工序 */
export function updateOp(data: MdmOpApi.Op) {
  return requestClient.put('/mdm/op/update', data);
}

/** 删除标准工序 */
export function deleteOp(id: number) {
  return requestClient.delete(`/mdm/op/delete?id=${id}`);
}

/** 批量删除标准工序 */
export function deleteOpList(ids: number[]) {
  return requestClient.delete(`/mdm/op/delete-list?ids=${ids.join(',')}`);
}

/** 导出标准工序 */
export function exportOp(params: any) {
  return requestClient.download('/mdm/op/export-excel', { params });
}
