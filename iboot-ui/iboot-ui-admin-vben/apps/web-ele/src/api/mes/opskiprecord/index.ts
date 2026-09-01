import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesOpSkipRecordApi {
  /**
   * MES-工序跳过记录。
   *
   * 是否允许跳序、是否需要审批由工序任务的跳序策略决定，后端按策略校验。
   */
  export interface OpSkipRecord {
    id?: number;
    opTaskId?: number;
    opTaskNo?: string;
    opName?: string;
    workOrderNo?: string;
    skipReasonType?: string;
    skipReason?: string;
    operatorId?: number;
    operatorName?: string;
    approvedBy?: number;
    approvedByName?: string;
    approveStatus?: string;
    skipTime?: number | string;
    effectRemark?: string;
    status?: string;
    remark?: string;
    createTime?: string;
  }
}

/** 查询工序跳过记录分页 */
export function getOpSkipRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MesOpSkipRecordApi.OpSkipRecord>>(
    '/mes/op-skip-record/page',
    { params },
  );
}

/** 查询工序跳过记录详情 */
export function getOpSkipRecord(id: number) {
  return requestClient.get<MesOpSkipRecordApi.OpSkipRecord>(
    `/mes/op-skip-record/get?id=${id}`,
  );
}

/** 新增工序跳过记录 */
export function createOpSkipRecord(data: MesOpSkipRecordApi.OpSkipRecord) {
  return requestClient.post('/mes/op-skip-record/create', data);
}

/** 修改工序跳过记录 */
export function updateOpSkipRecord(data: MesOpSkipRecordApi.OpSkipRecord) {
  return requestClient.put('/mes/op-skip-record/update', data);
}

/** 删除工序跳过记录 */
export function deleteOpSkipRecord(id: number) {
  return requestClient.delete(`/mes/op-skip-record/delete?id=${id}`);
}

/** 批量删除工序跳过记录 */
export function deleteOpSkipRecordList(ids: number[]) {
  return requestClient.delete(
    `/mes/op-skip-record/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出工序跳过记录 */
export function exportOpSkipRecord(params: any) {
  return requestClient.download('/mes/op-skip-record/export-excel', { params });
}
