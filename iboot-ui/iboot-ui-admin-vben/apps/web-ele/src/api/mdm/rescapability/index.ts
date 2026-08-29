import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmResCapabilityApi {
  /** 资源能力信息 */
  export interface ResCapability {
    id: number; // 主键
    resId?: number; // 生产资源编号
    resCode?: string; // 资源编码
    resName?: string; // 资源名称
    opId?: number; // 标准工序编号
    opCode?: string; // 工序编码
    opName?: string; // 工序名称
    preferredFlag?: boolean; // 是否首选资源
    efficiency?: number; // 专属效率系数
    status?: number; // 状态
  }
}

/** 查询资源能力分页 */
export function getResCapabilityPage(params: PageParam) {
  return requestClient.get<PageResult<MdmResCapabilityApi.ResCapability>>(
    '/mdm/res-capability/page',
    { params },
  );
}

/** 查询资源能力详情 */
export function getResCapability(id: number) {
  return requestClient.get<MdmResCapabilityApi.ResCapability>(
    `/mdm/res-capability/get?id=${id}`,
  );
}

/** 新增资源能力 */
export function createResCapability(data: MdmResCapabilityApi.ResCapability) {
  return requestClient.post('/mdm/res-capability/create', data);
}

/** 修改资源能力 */
export function updateResCapability(data: MdmResCapabilityApi.ResCapability) {
  return requestClient.put('/mdm/res-capability/update', data);
}

/** 删除资源能力 */
export function deleteResCapability(id: number) {
  return requestClient.delete(`/mdm/res-capability/delete?id=${id}`);
}

/** 批量删除资源能力 */
export function deleteResCapabilityList(ids: number[]) {
  return requestClient.delete(
    `/mdm/res-capability/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出资源能力 */
export function exportResCapability(params: any) {
  return requestClient.download('/mdm/res-capability/export-excel', { params });
}
