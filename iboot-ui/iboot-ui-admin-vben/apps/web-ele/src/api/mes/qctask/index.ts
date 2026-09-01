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

/** 新增质检任务 */
export function createQcTask(data: MesQcTaskApi.QcTask) {
  return requestClient.post('/mes/qc-task/create', data);
}

/** 修改质检任务 */
export function updateQcTask(data: MesQcTaskApi.QcTask) {
  return requestClient.put('/mes/qc-task/update', data);
}

/** 删除质检任务 */
export function deleteQcTask(id: number) {
  return requestClient.delete(`/mes/qc-task/delete?id=${id}`);
}

/** 批量删除质检任务 */
export function deleteQcTaskList(ids: number[]) {
  return requestClient.delete(`/mes/qc-task/delete-list?ids=${ids.join(',')}`);
}

/** 导出质检任务 */
export function exportQcTask(params: any) {
  return requestClient.download('/mes/qc-task/export-excel', { params });
}
