import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsProjectionMatApi {
  /** APS-MDM物料投影信息 */
  export interface ProjectionMat {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    sourceMatId?: number; // MDM物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    matSpec: string; // 物料规格快照
    matType?: string; // 物料类型快照
    baseUomId?: number; // 基准计量单位编号
    baseUomCode?: string; // 基准计量单位编码快照
    batchFlag?: boolean; // 是否批次管控
    bomId?: number; // 当前有效BOM编号，0表示无
    bomVersion: string; // 当前有效BOM版本
    routeId?: number; // 当前有效工艺路线编号，0表示无
    routeVersion: string; // 当前有效工艺路线版本
    sourceUpdateTime?: string | Dayjs; // 来源数据更新时间
    snapshotTime?: string | Dayjs; // 投影快照时间
    remark?: string; // 备注
  }
}

/** 查询APS-MDM物料投影分页 */
export function getProjectionMatPage(params: PageParam) {
  return requestClient.get<PageResult<ApsProjectionMatApi.ProjectionMat>>(
    '/aps/projection-mat/page',
    { params },
  );
}

/** 查询APS-MDM物料投影详情 */
export function getProjectionMat(id: number) {
  return requestClient.get<ApsProjectionMatApi.ProjectionMat>(
    `/aps/projection-mat/get?id=${id}`,
  );
}

/** 导出APS-MDM物料投影 */
export function exportProjectionMat(params: any) {
  return requestClient.download('/aps/projection-mat/export-excel', { params });
}


