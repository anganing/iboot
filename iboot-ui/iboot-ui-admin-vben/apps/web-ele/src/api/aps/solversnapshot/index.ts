import type { Dayjs } from 'dayjs';

import type { PageParam, PageResult } from '@vben/request';

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
    constraintMatch?: ConstraintMatch; // 解析后的约束命中对照表，仅约束命中类型的详情有值
    checksum?: string; // 快照内容校验值
    snapshotTime?: Dayjs | string; // 快照生成时间
    remark?: string; // 备注
  }

  /** 单条约束的命中情况 */
  export interface ConstraintMatchItem {
    constraintName: string; // 约束名
    displayName: string; // 约束中文名
    category?: string; // 约束类别：HARD、SOFT
    categoryName?: string; // 约束类别中文名
    description?: string; // 业务说明：罚什么、权重调大调小会怎样
    hardWeight?: number; // 本次求解生效的硬约束权重
    softWeight?: number; // 本次求解生效的软约束权重
    matchCount?: number; // 命中次数
    hardScore?: number; // 该约束扣掉的硬分
    softScore?: number; // 该约束扣掉的软分
  }

  /** 约束命中分析：解析后的对照表，回答"为什么是这个分数" */
  export interface ConstraintMatch {
    snapshotId: number; // 快照编号
    versionId: number; // 排程版本编号
    versionNo?: number; // 排程版本号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    snapshotTime?: Dayjs | string; // 快照生成时间
    hardScore?: number; // 硬约束总得分
    softScore?: number; // 软约束总得分
    constraints: ConstraintMatchItem[]; // 各约束的权重、命中次数与扣分
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

/** 查询某个排程版本的约束命中分析，没有命中快照时返回 null */
export function getConstraintMatch(versionId: number) {
  return requestClient.get<ApsSolverSnapshotApi.ConstraintMatch | null>(
    `/aps/solver-snapshot/constraint-match?versionId=${versionId}`,
  );
}

/** 导出APS-Timefold求解快照 */
export function exportSolverSnapshot(params: any) {
  return requestClient.download('/aps/solver-snapshot/export-excel', { params });
}


