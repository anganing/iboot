import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsVersionApi {
  /** APS-排程版本信息 */
  export interface Version {
    id?: number; // 主键
    scenarioId?: number; // 排程场景编号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    versionNo?: number; // 排程版本号
    status?: string; // 状态，字典 aps_version_status
    score?: string; // 求解器综合得分
    hardScore?: number; // 硬约束得分
    softScore?: number; // 软约束得分
    solveStartTime?: string; // 求解开始时间
    solveEndTime?: string; // 求解结束时间
    publishedTime?: string; // 发布时间
    publishedBy?: number; // 发布人员编号
    publishedByName?: string; // 发布人
    solverMessage?: string; // 求解结果或失败信息
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }

  /** 生成排程版本参数 */
  export interface GenerateReq {
    scenarioId: number; // 排程场景编号
    strategyId?: number; // 排程策略编号，留空则用场景上引用的策略
    remark?: string; // 备注
  }

  /** 求解进度日志 */
  export interface SolveProgressLog {
    status?: string; // 进度状态
    content?: string; // 日志内容
    occurTime?: string; // 发生时间
  }

  /** 求解进度，与 WebSocket 推送的结构一致 */
  export interface SolveProgress {
    versionId?: number; // 排程版本编号
    scenarioId?: number; // 排程场景编号
    status?: string; // 进度状态：PREPARING、SOLVING、SAVING、SUCCESS、FAILED、CANCELLED
    ratio?: number; // 进度百分比
    step?: number; // 当前步骤
    totalStep?: number; // 总步骤数
    score?: string; // 当前最优得分
    hardScore?: number; // 硬约束得分
    softScore?: number; // 软约束得分
    message?: string; // 最近一条进度说明
    startTime?: string; // 求解开始时间
    occurTime?: string; // 最近一次更新时间
    logs?: SolveProgressLog[]; // 进度日志
  }
}

/** 查询排程版本分页 */
export function getVersionPage(params: PageParam) {
  return requestClient.get<PageResult<ApsVersionApi.Version>>(
    '/aps/version/page',
    { params },
  );
}

/** 查询排程版本详情 */
export function getVersion(id: number) {
  return requestClient.get<ApsVersionApi.Version>(`/aps/version/get?id=${id}`);
}

/** 查询场景当前已发布的排程版本，用于车间侧直接看生效计划；未发布过返回 null */
export function getPublishedVersion(scenarioId: number) {
  return requestClient.get<ApsVersionApi.Version | null>(
    `/aps/version/get-published?scenarioId=${scenarioId}`,
  );
}

/** 查询场景下曾经生效过的排程版本（已发布与已被替换），用于车间侧回看历史计划 */
export function getEffectiveVersionList(scenarioId: number) {
  return requestClient.get<ApsVersionApi.Version[]>(
    `/aps/version/list-effective?scenarioId=${scenarioId}`,
  );
}

/** 生成排程版本并提交求解，返回新版本编号 */
export function generateVersion(data: ApsVersionApi.GenerateReq) {
  return requestClient.post<number>('/aps/version/generate', data);
}

/** 取消排程求解 */
export function cancelSolve(versionId: number) {
  return requestClient.put(`/aps/version/cancel-solve?versionId=${versionId}`);
}

/** 查询求解进度，用于页面刷新或断线重连后补齐已发生的进度 */
export function getSolveProgress(versionId: number) {
  return requestClient.get<ApsVersionApi.SolveProgress>(
    `/aps/version/solve-progress?versionId=${versionId}`,
  );
}

/** 确认排程版本 */
export function confirmVersion(id: number) {
  return requestClient.put(`/aps/version/confirm?id=${id}`);
}

/** 发布排程版本，同场景原已发布版本自动置为已被替换 */
export function publishVersion(id: number) {
  return requestClient.put(`/aps/version/publish?id=${id}`);
}

/** 导出排程版本 */
export function exportVersion(params: any) {
  return requestClient.download('/aps/version/export-excel', { params });
}
