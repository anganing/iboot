import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesOpSkipRecordApi {
  /** MES-工序跳过记录信息 */
  export interface OpSkipRecord {
    id: number; // 主键
    opTaskId?: number; // 被跳过工序任务编号
    skipReasonType?: string; // 跳过原因类型
    skipReason?: string; // 跳过原因
    operatorId?: number; // 操作人员编号
    approvedBy: number; // 审批人员编号
    approveStatus?: string; // 审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED
    skipTime: string | Dayjs; // 实际跳过时间
    effectRemark: string; // 对后续工序、质检及排程的影响说明
    status?: string; // 状态：PENDING、EFFECTIVE、REVOKED
    remark: string; // 备注
  }
}

/** 查询MES-工序跳过记录分页 */
export function getOpSkipRecordPage(params: PageParam) {
  return requestClient.get<PageResult<MesOpSkipRecordApi.OpSkipRecord>>(
    '/mes/op-skip-record/page',
    { params },
  );
}

/** 查询MES-工序跳过记录详情 */
export function getOpSkipRecord(id: number) {
  return requestClient.get<MesOpSkipRecordApi.OpSkipRecord>(
    `/mes/op-skip-record/get?id=${id}`,
  );
}

/** 新增MES-工序跳过记录 */
export function createOpSkipRecord(data: MesOpSkipRecordApi.OpSkipRecord) {
  return requestClient.post('/mes/op-skip-record/create', data);
}

/** 修改MES-工序跳过记录 */
export function updateOpSkipRecord(data: MesOpSkipRecordApi.OpSkipRecord) {
  return requestClient.put('/mes/op-skip-record/update', data);
}

/** 删除MES-工序跳过记录 */
export function deleteOpSkipRecord(id: number) {
  return requestClient.delete(`/mes/op-skip-record/delete?id=${id}`);
}

/** 批量删除MES-工序跳过记录 */
export function deleteOpSkipRecordList(ids: number[]) {
  return requestClient.delete(
    `/mes/op-skip-record/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-工序跳过记录 */
export function exportOpSkipRecord(params: any) {
  return requestClient.download('/mes/op-skip-record/export-excel', { params });
}

/** 下载MES-工序跳过记录导入模板 */
export function importOpSkipRecordTemplate() {
  return requestClient.download('/mes/op-skip-record/get-import-template');
}

/** 导入MES-工序跳过记录 */
export function importOpSkipRecord(file: File) {
  return requestClient.upload('/mes/op-skip-record/import', { file });
}

