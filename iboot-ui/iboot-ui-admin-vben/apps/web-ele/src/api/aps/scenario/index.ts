import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsScenarioApi {
  /** APS-排程场景信息 */
  export interface Scenario {
    id: number; // 主键
    code?: string; // 排程场景编码
    name?: string; // 排程场景名称
    plantId?: number; // 工厂编号
    workshopId?: number; // 车间编号，0表示全工厂
    strategyId?: number; // 排程策略编号
    planStartTime?: string | Dayjs; // 计划窗口开始时间
    planEndTime?: string | Dayjs; // 计划窗口结束时间
    snapshotTime: string | Dayjs; // 输入数据快照时间
    status?: string; // 状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED
    remark: string; // 备注
  }
}

/** 查询APS-排程场景分页 */
export function getScenarioPage(params: PageParam) {
  return requestClient.get<PageResult<ApsScenarioApi.Scenario>>(
    '/aps/scenario/page',
    { params },
  );
}

/** 查询APS-排程场景详情 */
export function getScenario(id: number) {
  return requestClient.get<ApsScenarioApi.Scenario>(
    `/aps/scenario/get?id=${id}`,
  );
}

/** 新增APS-排程场景 */
export function createScenario(data: ApsScenarioApi.Scenario) {
  return requestClient.post('/aps/scenario/create', data);
}

/** 修改APS-排程场景 */
export function updateScenario(data: ApsScenarioApi.Scenario) {
  return requestClient.put('/aps/scenario/update', data);
}

/** 删除APS-排程场景 */
export function deleteScenario(id: number) {
  return requestClient.delete(`/aps/scenario/delete?id=${id}`);
}

/** 批量删除APS-排程场景 */
export function deleteScenarioList(ids: number[]) {
  return requestClient.delete(
    `/aps/scenario/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程场景 */
export function exportScenario(params: any) {
  return requestClient.download('/aps/scenario/export-excel', { params });
}

/** 下载APS-排程场景导入模板 */
export function importScenarioTemplate() {
  return requestClient.download('/aps/scenario/get-import-template');
}

/** 导入APS-排程场景 */
export function importScenario(file: File) {
  return requestClient.upload('/aps/scenario/import', { file });
}

