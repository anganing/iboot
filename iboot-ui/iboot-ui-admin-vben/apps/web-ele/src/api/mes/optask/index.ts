import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace MesOpTaskApi {
  /** MES-工序任务信息 */
  export interface OpTask {
    id: number; // 主键
    taskNo?: string; // 工序任务号
    workOrderId?: number; // 作业工单编号
    workOrderNo?: string; // 作业工单号快照
    routeOpId?: number; // 锁定的路线工序编号
    opId?: number; // 标准工序编号
    opCode?: string; // 工序编码快照
    opName?: string; // 工序名称快照
    seq?: number; // 工序顺序号
    parallelGroup: string; // 并行工序组
    prevTaskId: number; // 前序工序任务编号
    nextTaskId: number; // 后序工序任务编号
    execMode?: string; // 执行模式：SEQUENTIAL、PARALLEL、OPTIONAL
    qcMode?: string; // 质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE
    skipPolicy?: string; // 跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED
    keyFlag?: boolean; // 是否关键工序
    plannedQty?: number; // 计划数量
    completedQty?: number; // 合格完成数量
    scrapQty?: number; // 报废数量
    resId: number; // 当前分配资源编号
    plannedStartTime: string | Dayjs; // 计划开始时间
    plannedEndTime: string | Dayjs; // 计划结束时间
    actualStartTime: string | Dayjs; // 实际开始时间
    actualEndTime: string | Dayjs; // 实际结束时间
    status?: string; // 状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询MES-工序任务分页 */
export function getOpTaskPage(params: PageParam) {
  return requestClient.get<PageResult<MesOpTaskApi.OpTask>>(
    '/mes/op-task/page',
    { params },
  );
}

/** 查询MES-工序任务详情 */
export function getOpTask(id: number) {
  return requestClient.get<MesOpTaskApi.OpTask>(
    `/mes/op-task/get?id=${id}`,
  );
}

/** 新增MES-工序任务 */
export function createOpTask(data: MesOpTaskApi.OpTask) {
  return requestClient.post('/mes/op-task/create', data);
}

/** 修改MES-工序任务 */
export function updateOpTask(data: MesOpTaskApi.OpTask) {
  return requestClient.put('/mes/op-task/update', data);
}

/** 删除MES-工序任务 */
export function deleteOpTask(id: number) {
  return requestClient.delete(`/mes/op-task/delete?id=${id}`);
}

/** 批量删除MES-工序任务 */
export function deleteOpTaskList(ids: number[]) {
  return requestClient.delete(
    `/mes/op-task/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出MES-工序任务 */
export function exportOpTask(params: any) {
  return requestClient.download('/mes/op-task/export-excel', { params });
}

/** 下载MES-工序任务导入模板 */
export function importOpTaskTemplate() {
  return requestClient.download('/mes/op-task/get-import-template');
}

/** 导入MES-工序任务 */
export function importOpTask(file: File) {
  return requestClient.upload('/mes/op-task/import', { file });
}

