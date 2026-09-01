import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsSolverSnapshotApi {
  /** APS-Timefold求解快照信息 */
  export interface SolverSnapshot {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    versionNo?: number; // 排程版本号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    snapshotType?: string; // 快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR
    snapshotJson?: string; // Timefold求解输入、结果或约束命中快照
    checksum?: string; // 快照内容校验值
    snapshotTime?: string | Dayjs; // 快照生成时间
    remark?: string; // 备注
  }
}

/** 查询APS-Timefold求解快照分页 */
export function getSolverSnapshotPage(params: PageParam) {
  return requestClient.get<PageResult<ApsSolverSnapshotApi.SolverSnapshot>>(
    '/aps/solver-snapshot/page',
    { params },
  );
}

/** 查询APS-Timefold求解快照详情 */
export function getSolverSnapshot(id: number) {
  return requestClient.get<ApsSolverSnapshotApi.SolverSnapshot>(
    `/aps/solver-snapshot/get?id=${id}`,
  );
}

/** 导出APS-Timefold求解快照 */
export function exportSolverSnapshot(params: any) {
  return requestClient.download('/aps/solver-snapshot/export-excel', { params });
}


