import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsProjectionResourceApi {
  /** APS-MDM资源与日历投影信息 */
  export interface ProjectionResource {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    sourceResId?: number; // MDM生产资源编号
    resCode?: string; // 资源编码快照
    resName?: string; // 资源名称快照
    resType?: string; // 资源类型快照
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号
    wcId?: number; // 工作中心编号
    workDate?: string | Dayjs; // 产能日期
    shiftId?: number; // 班次编号，0表示日汇总
    availableMinutes?: number; // 可用分钟数
    capabilityJson: string; // 资源能力快照
    sourceUpdateTime?: string | Dayjs; // 来源数据更新时间
    snapshotTime?: string | Dayjs; // 投影快照时间
    remark?: string; // 备注
  }
}

/** 查询APS-MDM资源与日历投影分页 */
export function getProjectionResourcePage(params: PageParam) {
  return requestClient.get<PageResult<ApsProjectionResourceApi.ProjectionResource>>(
    '/aps/projection-resource/page',
    { params },
  );
}

/** 查询APS-MDM资源与日历投影详情 */
export function getProjectionResource(id: number) {
  return requestClient.get<ApsProjectionResourceApi.ProjectionResource>(
    `/aps/projection-resource/get?id=${id}`,
  );
}

/** 导出APS-MDM资源与日历投影 */
export function exportProjectionResource(params: any) {
  return requestClient.download('/aps/projection-resource/export-excel', { params });
}


