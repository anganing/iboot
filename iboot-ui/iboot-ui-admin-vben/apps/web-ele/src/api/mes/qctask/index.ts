import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesQcTaskApi {
  /**
   * MES-质检任务。
   *
   * 方案版本、物料与单位、批次号快照由后端补全；合格与不合格数量由检验结果汇总，前端只读。
   */
  export interface QcTask {
    id?: number;
    taskNo?: string;
    ruleId?: number;
    ruleCode?: string;
    ruleName?: string;
    qcPlanId?: number;
    qcPlanCode?: string;
    qcPlanName?: string;
    qcPlanVersion?: string;
    triggerPoint?: string;
    bizType?: string;
    bizNo?: string;
    bizLineId?: number;
    matId?: number;
    matCode?: string;
    matName?: string;
    matSpec?: string;
    batchId?: number;
    lotNo?: string;
    qty?: number;
    sampleQty?: number;
    uomId?: number;
    uomCode?: string;
    assignedInspectorId?: number;
    assignedInspectorName?: string;
    inspectStartTime?: number | string;
    inspectEndTime?: number | string;
    conclusion?: string;
    qtyQualified?: number;
    qtyRejected?: number;
    status?: string;
    idempotencyKey?: string;
    version?: number;
    remark?: string;
    createTime?: string;
  }
}

/** 查询质检任务分页 */
export function getQcTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesQcTaskApi.QcTask>>(
    '/mes/qc-task/page',
    { params },
  );
}

/** 查询质检任务详情 */
export function getQcTask(id: number) {
  return requestClient.get<MesQcTaskApi.QcTask>(`/mes/qc-task/get?id=${id}`);
}

/** 指派检验员 */
export function assignQcTask(id: number, inspectorId: number) {
  return requestClient.post('/mes/qc-task/assign', { id, inspectorId });
}

/** 开始检验 */
export function startInspectQcTask(id: number) {
  return requestClient.post('/mes/qc-task/start-inspect', { id });
}

/** 提交检验结论 */
export function submitQcTaskConclusion(data: {
  id: number;
  conclusion: string;
  qtyQualified: number;
  qtyRejected: number;
}) {
  return requestClient.post('/mes/qc-task/submit-conclusion', data);
}

/** 取消质检任务 */
export function cancelQcTask(id: number, remark?: string) {
  return requestClient.post('/mes/qc-task/cancel', { id, remark });
}

/** 导出质检任务 */
export function exportQcTask(params: any) {
  return requestClient.download('/mes/qc-task/export-excel', { params });
}
