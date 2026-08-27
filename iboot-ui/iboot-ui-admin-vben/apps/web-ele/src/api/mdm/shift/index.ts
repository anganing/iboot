import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MdmShiftApi {
  /** 班次信息 */
  export interface Shift {
    id: number; // 主键
    code?: string; // 编码
    name?: string; // 名称
    startTime?: string; // 开始时间
    endTime?: string; // 结束时间
    status?: number; // 状态
    remark: string; // 备注
  }
}

/** 查询班次分页 */
export function getShiftPage(params: PageParam) {
  return requestClient.get<PageResult<MdmShiftApi.Shift>>('/mdm/shift/page', {
    params,
  });
}

/** 查询班次详情 */
export function getShift(id: number) {
  return requestClient.get<MdmShiftApi.Shift>(`/mdm/shift/get?id=${id}`);
}

/** 新增班次 */
export function createShift(data: MdmShiftApi.Shift) {
  return requestClient.post('/mdm/shift/create', data);
}

/** 修改班次 */
export function updateShift(data: MdmShiftApi.Shift) {
  return requestClient.put('/mdm/shift/update', data);
}

/** 删除班次 */
export function deleteShift(id: number) {
  return requestClient.delete(`/mdm/shift/delete?id=${id}`);
}

/** 批量删除班次 */
export function deleteShiftList(ids: number[]) {
  return requestClient.delete(`/mdm/shift/delete-list?ids=${ids.join(',')}`);
}

/** 导出班次 */
export function exportShift(params: any) {
  return requestClient.download('/mdm/shift/export-excel', { params });
}
