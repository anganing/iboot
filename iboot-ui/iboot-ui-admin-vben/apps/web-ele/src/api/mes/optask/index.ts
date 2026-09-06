import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MesOpTaskApi {
  /**
   * MES-工序任务。
   *
   * 作业工单号、工序编码与名称、工序顺序、并行组、关键工序标记、执行模式都由后端从作业工单与
   * 路线工序带出；完成与报废数量由报工推进；前后序任务由工序链生成时维护。这些字段前端只读。
   */
  export interface OpTask {
    id?: number;
    taskNo?: string;
    workOrderId?: number;
    workOrderNo?: string;
    prodOrderNo?: string;
    routeOpId?: number;
    opId?: number;
    opCode?: string;
    opName?: string;
    seq?: number;
    parallelGroup?: string;
    prevTaskId?: number;
    nextTaskId?: number;
    execMode?: string;
    qcMode?: string;
    skipPolicy?: string;
    keyFlag?: boolean;
    plannedQty?: number;
    completedQty?: number;
    scrapQty?: number;
    resId?: number;
    resName?: string;
    plannedStartTime?: number | string;
    plannedEndTime?: number | string;
    actualStartTime?: string;
    actualEndTime?: string;
    status?: string;
    version?: number;
    remark?: string;
    createTime?: string;
  }

  export interface OpTaskActionReq {
    id: number;
    idempotencyKey?: string;
    operatorId?: number;
    reason?: string;
  }

  export interface OpTaskFinishReq extends OpTaskActionReq {
    qualifiedQty?: number;
    scrapQty?: number;
  }

  export interface OpTaskSkipReq extends OpTaskActionReq {
    skipReasonType: string;
    effectRemark?: string;
  }

  export interface OpTaskRevokeSkipReq {
    skipRecordId: number;
    idempotencyKey?: string;
    reason?: string;
  }
}

/** 查询工序任务分页 */
export function getOpTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesOpTaskApi.OpTask>>(
    '/mes/op-task/page',
    { params },
  );
}

/** 查询工序任务详情 */
export function getOpTask(id: number) {
  return requestClient.get<MesOpTaskApi.OpTask>(`/mes/op-task/get?id=${id}`);
}

/** 导出工序任务 */
export function exportOpTask(params: any) {
  return requestClient.download('/mes/op-task/export-excel', { params });
}

/** 工序开工 */
export function startOpTask(data: MesOpTaskApi.OpTaskActionReq) {
  return requestClient.post('/mes/op-task/start', data);
}

/** 工序暂停 */
export function pauseOpTask(data: MesOpTaskApi.OpTaskActionReq) {
  return requestClient.post('/mes/op-task/pause', data);
}

/** 工序恢复 */
export function resumeOpTask(data: MesOpTaskApi.OpTaskActionReq) {
  return requestClient.post('/mes/op-task/resume', data);
}

/** 工序完工 */
export function finishOpTask(data: MesOpTaskApi.OpTaskFinishReq) {
  return requestClient.post('/mes/op-task/finish', data);
}

/** 工序跳序 */
export function skipOpTask(data: MesOpTaskApi.OpTaskSkipReq) {
  return requestClient.post('/mes/op-task/skip', data);
}

/** 撤销跳序 */
export function revokeSkipOpTask(
  data: MesOpTaskApi.OpTaskRevokeSkipReq,
) {
  return requestClient.post('/mes/op-task/revoke-skip', data);
}
