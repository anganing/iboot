import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesQcTaskApi {
  /** MES-质检任务信息 */
  export interface QcTask {
    id: number; // 主键
    taskNo?: string; // 质检任务号
    ruleId: number; // 命中的质检规则编号
    qcPlanId?: number; // 锁定的检验方案编号
    qcPlanVersion?: string; // 锁定的检验方案版本
    triggerPoint?: string; // 触发点：IQC、IPQC、FQC、OQC、FAI
    bizType?: string; // 来源业务类型
    bizNo?: string; // 来源业务单号
    bizLineId: number; // 来源业务明细编号
    matId?: number; // 检验物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    batchId: number; // 检验批次编号
    lotNo: string; // 批次号快照
    qty?: number; // 送检数量
    sampleQty?: number; // 抽样数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    assignedInspectorId: number; // 指派检验员编号
    inspectStartTime: string | Dayjs; // 检验开始时间
    inspectEndTime: string | Dayjs; // 检验结束时间
    conclusion: string; // 检验结论：QUALIFIED、REJECTED、CONCESSION
    qtyQualified?: number; // 合格数量
    qtyRejected?: number; // 不合格数量
    status?: string; // 状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED
    idempotencyKey: string; // 创建幂等键
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询MES-质检任务分页 */
export function getQcTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcTaskApi.QcTask>>(
    '/mes/qc-task/page',
    { params },
  );
}

/** 查询MES-质检任务详情 */
export function getQcTask(id: number) {
  return requestClient.get<MesQcTaskApi.QcTask>(
    `/mes/qc-task/get?id=${id}`,
  );
}

/** 新增MES-质检任务 */
export function createQcTask(data: MesQcTaskApi.QcTask) {
  return requestClient.post('/mes/qc-task/create', data);
}

/** 修改MES-质检任务 */
export function updateQcTask(data: MesQcTaskApi.QcTask) {
  return requestClient.put('/mes/qc-task/update', data);
}

/** 删除MES-质检任务 */
export function deleteQcTask(id: number) {
  return requestClient.delete(`/mes/qc-task/delete?id=${id}`);
}

/** 批量删除MES-质检任务 */
export function deleteQcTaskList(ids: number[]) {
  return requestClient.delete(
    `/mes/qc-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-质检任务 */
export function exportQcTask(params: any) {
  return requestClient.download('/mes/qc-task/export-excel', { params });
}

/** 下载MES-质检任务导入模板 */
export function importQcTaskTemplate() {
  return requestClient.download('/mes/qc-task/get-import-template');
}

/** 导入MES-质检任务 */
export function importQcTask(file: File) {
  return requestClient.upload('/mes/qc-task/import', { file });
}

