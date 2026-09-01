import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsProjectionInvApi {
  /** APS-WMS库存投影信息 */
  export interface ProjectionInv {
    id: number; // 主键
    scenarioId?: number; // 排程场景编号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    sourceBalanceId?: number; // WMS库存余额编号
    invOrgId?: number; // 库存组织编号
    invOrgName?: string; // 库存组织名称
    plantId?: number; // 工厂编号
    plantName?: string; // 工厂名称
    whId?: number; // 仓库编号
    whName?: string; // 仓库名称
    locId?: number; // 库位编号
    locName?: string; // 库位编码
    matId?: number; // 物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称
    batchId?: number; // 批次编号
    lotNo?: string; // 批次号快照
    projectId?: number; // 项目编号，0表示无项目
    projectName?: string; // 项目名称
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    stockScope?: string; // 库存范围：COMMON、PROJECT_DEDICATED
    qualityStatus?: string; // 质量状态
    qtyOnhand?: number; // 现存数量快照
    qtyReserved?: number; // 预留数量快照
    qtyFrozen?: number; // 冻结数量快照
    qtyAvailable?: number; // 可用数量快照
    sourceUpdateTime?: string | Dayjs; // 来源数据更新时间
    snapshotTime?: string | Dayjs; // 投影快照时间
    remark?: string; // 备注
  }
}

/** 查询APS-WMS库存投影分页 */
export function getProjectionInvPage(params: PageParam) {
  return requestClient.get<PageResult<ApsProjectionInvApi.ProjectionInv>>(
    '/aps/projection-inv/page',
    { params },
  );
}

/** 查询APS-WMS库存投影详情 */
export function getProjectionInv(id: number) {
  return requestClient.get<ApsProjectionInvApi.ProjectionInv>(
    `/aps/projection-inv/get?id=${id}`,
  );
}

/** 导出APS-WMS库存投影 */
export function exportProjectionInv(params: any) {
  return requestClient.download('/aps/projection-inv/export-excel', { params });
}


