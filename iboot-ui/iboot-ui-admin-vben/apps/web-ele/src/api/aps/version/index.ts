import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsVersionApi {
  /** APS-排程版本信息 */
  export interface Version {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    versionNo?: number; // 排程版本号
    status?: string; // 状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED
    score: string; // Timefold综合得分
    hardScore: number; // 硬约束得分
    softScore: number; // 软约束得分
    solveStartTime: string | Dayjs; // 求解开始时间
    solveEndTime: string | Dayjs; // 求解结束时间
    publishedTime: string | Dayjs; // 发布时间
    publishedBy: number; // 发布人员编号
    solverMessage: string; // 求解结果或失败信息
    version?: number; // 乐观锁版本
    remark: string; // 备注
  }
}

/** 查询APS-排程版本分页 */
export function getVersionPage(params: PageParam) {
  return requestClient.get<PageResult<ApsVersionApi.Version>>(
    '/aps/version/page',
    { params },
  );
}

/** 查询APS-排程版本详情 */
export function getVersion(id: number) {
  return requestClient.get<ApsVersionApi.Version>(
    `/aps/version/get?id=${id}`,
  );
}

/** 新增APS-排程版本 */
export function createVersion(data: ApsVersionApi.Version) {
  return requestClient.post('/aps/version/create', data);
}

/** 修改APS-排程版本 */
export function updateVersion(data: ApsVersionApi.Version) {
  return requestClient.put('/aps/version/update', data);
}

/** 删除APS-排程版本 */
export function deleteVersion(id: number) {
  return requestClient.delete(`/aps/version/delete?id=${id}`);
}

/** 批量删除APS-排程版本 */
export function deleteVersionList(ids: number[]) {
  return requestClient.delete(
    `/aps/version/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程版本 */
export function exportVersion(params: any) {
  return requestClient.download('/aps/version/export-excel', { params });
}

/** 下载APS-排程版本导入模板 */
export function importVersionTemplate() {
  return requestClient.download('/aps/version/get-import-template');
}

/** 导入APS-排程版本 */
export function importVersion(file: File) {
  return requestClient.upload('/aps/version/import', { file });
}

