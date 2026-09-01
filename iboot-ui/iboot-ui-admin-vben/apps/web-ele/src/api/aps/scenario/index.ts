import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsScenarioApi {
  /** APS-排程场景信息 */
  export interface Scenario {
    id?: number; // 主键
    code?: string; // 排程场景编码，留空由编码规则分配
    name?: string; // 排程场景名称
    plantId?: number; // 工厂编号
    plantName?: string; // 工厂名称
    workshopId?: number; // 车间编号，0 表示全工厂
    workshopName?: string; // 车间名称
    strategyId?: number; // 排程策略编号
    strategyCode?: string; // 排程策略编码
    strategyName?: string; // 排程策略名称
    planStartTime?: string; // 计划窗口开始时间
    planEndTime?: string; // 计划窗口结束时间
    snapshotTime?: string; // 输入数据快照时间
    status?: string; // 状态，字典 aps_scenario_status
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }
}

/** 查询排程场景分页 */
export function getScenarioPage(params: PageParam) {
  return requestClient.get<PageResult<ApsScenarioApi.Scenario>>(
    '/aps/scenario/page',
    { params },
  );
}

/** 查询排程场景详情 */
export function getScenario(id: number) {
  return requestClient.get<ApsScenarioApi.Scenario>(
    `/aps/scenario/get?id=${id}`,
  );
}

/** 新增排程场景 */
export function createScenario(data: ApsScenarioApi.Scenario) {
  return requestClient.post('/aps/scenario/create', data);
}

/** 修改排程场景 */
export function updateScenario(data: ApsScenarioApi.Scenario) {
  return requestClient.put('/aps/scenario/update', data);
}

/** 关闭排程场景，关闭后不再允许刷新输入与生成版本 */
export function closeScenario(id: number) {
  return requestClient.put(`/aps/scenario/close?id=${id}`);
}

/** 刷新排程输入投影，物料、库存、资源、工单四张投影一起重建 */
export function refreshScenarioProjection(id: number) {
  return requestClient.put(`/aps/scenario/refresh-projection?id=${id}`);
}

/** 删除排程场景 */
export function deleteScenario(id: number) {
  return requestClient.delete(`/aps/scenario/delete?id=${id}`);
}

/** 批量删除排程场景 */
export function deleteScenarioList(ids: number[]) {
  return requestClient.delete(`/aps/scenario/delete-list?ids=${ids.join(',')}`);
}

/** 导出排程场景 */
export function exportScenario(params: any) {
  return requestClient.download('/aps/scenario/export-excel', { params });
}

