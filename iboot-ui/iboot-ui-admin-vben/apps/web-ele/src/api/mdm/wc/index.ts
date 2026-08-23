import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmWcApi {
  /** 工作中心信息 */
  export interface Wc {
    id: number; // 主键
    workshopId?: number; // 车间编号
    code?: string; // 编码
    name?: string; // 名称
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询工作中心分页 */
export function getWcPage(params: PageParam) {
  return requestClient.get<PageResult<MdmWcApi.Wc>>(
    '/mdm/wc/page',
    { params },
  );
}

/** 查询工作中心详情 */
export function getWc(id: number) {
  return requestClient.get<MdmWcApi.Wc>(
    `/mdm/wc/get?id=${id}`,
  );
}

/** 新增工作中心 */
export function createWc(data: MdmWcApi.Wc) {
  return requestClient.post('/mdm/wc/create', data);
}

/** 修改工作中心 */
export function updateWc(data: MdmWcApi.Wc) {
  return requestClient.put('/mdm/wc/update', data);
}

/** 删除工作中心 */
export function deleteWc(id: number) {
  return requestClient.delete(`/mdm/wc/delete?id=${id}`);
}

/** 批量删除工作中心 */
export function deleteWcList(ids: number[]) {
  return requestClient.delete(
    `/mdm/wc/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出工作中心 */
export function exportWc(params: any) {
  return requestClient.download('/mdm/wc/export-excel', { params });
}


