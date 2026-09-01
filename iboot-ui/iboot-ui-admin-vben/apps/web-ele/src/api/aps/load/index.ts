import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsLoadApi {
  /** APS-资源负荷信息 */
  export interface Load {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    versionNo?: number; // 排程版本号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    resId?: number; // 生产资源编号
    resCode?: string; // 资源编码快照
    resName?: string; // 资源名称快照
    workDate?: string | Dayjs; // 负荷日期
    loadMinutes?: number; // 计划负荷分钟数
    capacityMinutes?: number; // 可用产能分钟数
    loadRate?: number; // 负荷率
    overloadMinutes?: number; // 超负荷分钟数
    remark?: string; // 备注
  }
}

/** 查询APS-资源负荷分页 */
export function getLoadPage(params: PageParam) {
  return requestClient.get<PageResult<ApsLoadApi.Load>>(
    '/aps/load/page',
    { params },
  );
}

/** 查询APS-资源负荷详情 */
export function getLoad(id: number) {
  return requestClient.get<ApsLoadApi.Load>(
    `/aps/load/get?id=${id}`,
  );
}

/** 按排程版本重算资源负荷 */
export function refreshLoad(versionId: number) {
  return requestClient.put(`/aps/load/refresh?versionId=${versionId}`);
}

/** 导出APS-资源负荷 */
export function exportLoad(params: any) {
  return requestClient.download('/aps/load/export-excel', { params });
}


