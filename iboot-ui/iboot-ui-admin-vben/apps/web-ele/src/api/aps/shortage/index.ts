import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsShortageApi {
  /** APS-排程缺料清单信息 */
  export interface Shortage {
    id: number; // 主键
    versionId?: number; // 排程版本编号
    prodOrderId?: number; // 生产工单编号，0表示汇总缺料
    prodOrderNo?: string; // 生产工单号
    opTaskId?: number; // 工序任务编号，0表示非工序级缺料
    projectId?: number; // 项目编号，0表示无项目
    projectTaskId?: number; // 项目任务编号，0表示无项目任务
    matId?: number; // 短缺物料编号
    matCode?: string; // 物料编码快照
    matName?: string; // 物料名称快照
    reqQty?: number; // 需求数量
    availQty?: number; // 可用数量
    shortQty?: number; // 短缺数量
    uomId?: number; // 计量单位编号
    uomCode?: string; // 计量单位编码快照
    needDate?: string | Dayjs; // 需求到位时间
    expectedSupplyDate?: string | Dayjs; // 预计供给时间
    versionNo?: number; // 排程版本号
    scenarioCode?: string; // 排程场景编码
    scenarioName?: string; // 排程场景名称
    projectName?: string; // 项目名称
    projectTaskName?: string; // 项目任务名称
    status?: string; // 状态：OPEN、PARTIAL、RESOLVED、IGNORED
    remark?: string; // 备注
  }
}

/** 查询APS-排程缺料清单分页 */
export function getShortagePage(params: PageParam) {
  return requestClient.get<PageResult<ApsShortageApi.Shortage>>(
    '/aps/shortage/page',
    { params },
  );
}

/** 查询APS-排程缺料清单详情 */
export function getShortage(id: number) {
  return requestClient.get<ApsShortageApi.Shortage>(
    `/aps/shortage/get?id=${id}`,
  );
}

/** 按排程版本刷新缺料清单 */
export function refreshShortage(versionId: number) {
  return requestClient.put(`/aps/shortage/refresh?versionId=${versionId}`);
}

/** 处理缺料，补充预计供给时间 */
export function handleShortage(data: {
  id: number;
  expectedSupplyDate?: string | Dayjs;
  remark?: string;
}) {
  return requestClient.put('/aps/shortage/handle', data);
}

/** 忽略缺料 */
export function ignoreShortage(id: number) {
  return requestClient.put(`/aps/shortage/ignore?id=${id}`);
}

/** 导出APS-排程缺料清单 */
export function exportShortage(params: any) {
  return requestClient.download('/aps/shortage/export-excel', { params });
}


