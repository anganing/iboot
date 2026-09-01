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
