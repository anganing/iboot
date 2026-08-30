import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsSolverSnapshotApi {
  /** APS-Timefold求解快照信息 */
  export interface SolverSnapshot {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    snapshotType?: string; // 快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR
    snapshotJson?: string; // Timefold求解输入、结果或约束命中快照
    checksum?: string; // 快照内容校验值
    snapshotTime?: string | Dayjs; // 快照生成时间
    remark: string; // 备注
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

/** 新增APS-Timefold求解快照 */
export function createSolverSnapshot(data: ApsSolverSnapshotApi.SolverSnapshot) {
  return requestClient.post('/aps/solver-snapshot/create', data);
}

/** 修改APS-Timefold求解快照 */
export function updateSolverSnapshot(data: ApsSolverSnapshotApi.SolverSnapshot) {
  return requestClient.put('/aps/solver-snapshot/update', data);
}

/** 删除APS-Timefold求解快照 */
export function deleteSolverSnapshot(id: number) {
  return requestClient.delete(`/aps/solver-snapshot/delete?id=${id}`);
}

/** 批量删除APS-Timefold求解快照 */
export function deleteSolverSnapshotList(ids: number[]) {
  return requestClient.delete(
    `/aps/solver-snapshot/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-Timefold求解快照 */
export function exportSolverSnapshot(params: any) {
  return requestClient.download('/aps/solver-snapshot/export-excel', { params });
}

/** 下载APS-Timefold求解快照导入模板 */
export function importSolverSnapshotTemplate() {
  return requestClient.download('/aps/solver-snapshot/get-import-template');
}

/** 导入APS-Timefold求解快照 */
export function importSolverSnapshot(file: File) {
  return requestClient.upload('/aps/solver-snapshot/import', { file });
}

