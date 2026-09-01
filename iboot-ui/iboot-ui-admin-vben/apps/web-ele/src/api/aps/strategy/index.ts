import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsStrategyApi {
  /** APS-排程策略信息 */
  export interface Strategy {
    id?: number; // 主键
    code?: string; // 排程策略编码，留空由编码规则分配
    name?: string; // 排程策略名称
    versionNo?: string; // 策略版本号
    objective?: string; // 优化目标说明
    configJson?: string; // 硬约束、软约束及权重配置
    status?: string; // 状态，字典 aps_strategy_status
    remark?: string; // 备注
    createTime?: string; // 创建时间
  }
}

/** 查询排程策略分页 */
export function getStrategyPage(params: PageParam) {
  return requestClient.get<PageResult<ApsStrategyApi.Strategy>>(
    '/aps/strategy/page',
    { params },
  );
}

/** 查询排程策略详情 */
export function getStrategy(id: number) {
  return requestClient.get<ApsStrategyApi.Strategy>(
    `/aps/strategy/get?id=${id}`,
  );
}

/** 新增排程策略 */
export function createStrategy(data: ApsStrategyApi.Strategy) {
  return requestClient.post('/aps/strategy/create', data);
}

/** 修改排程策略 */
export function updateStrategy(data: ApsStrategyApi.Strategy) {
  return requestClient.put('/aps/strategy/update', data);
}

/** 删除排程策略 */
export function deleteStrategy(id: number) {
  return requestClient.delete(`/aps/strategy/delete?id=${id}`);
}

/** 批量删除排程策略 */
export function deleteStrategyList(ids: number[]) {
  return requestClient.delete(`/aps/strategy/delete-list?ids=${ids.join(',')}`);
}

/** 导出排程策略 */
export function exportStrategy(params: any) {
  return requestClient.download('/aps/strategy/export-excel', { params });
}
