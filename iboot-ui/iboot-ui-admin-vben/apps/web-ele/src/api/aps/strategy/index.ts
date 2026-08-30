import type { PageParam, PageResult } from '@vben/request';
import type { Dayjs } from 'dayjs';

import { requestClient } from '#/api/request';

export namespace ApsStrategyApi {
  /** APS-排程策略信息 */
  export interface Strategy {
    id: number; // 主键
    code?: string; // 排程策略编码
    name?: string; // 排程策略名称
    versionNo?: string; // 策略版本号
    objective?: string; // 优化目标说明
    configJson?: string; // 硬约束、软约束及权重配置
    status?: string; // 状态：DRAFT、ENABLED、DISABLED
    remark: string; // 备注
  }
}

/** 查询APS-排程策略分页 */
export function getStrategyPage(params: PageParam) {
  return requestClient.get<PageResult<ApsStrategyApi.Strategy>>(
    '/aps/strategy/page',
    { params },
  );
}

/** 查询APS-排程策略详情 */
export function getStrategy(id: number) {
  return requestClient.get<ApsStrategyApi.Strategy>(
    `/aps/strategy/get?id=${id}`,
  );
}

/** 新增APS-排程策略 */
export function createStrategy(data: ApsStrategyApi.Strategy) {
  return requestClient.post('/aps/strategy/create', data);
}

/** 修改APS-排程策略 */
export function updateStrategy(data: ApsStrategyApi.Strategy) {
  return requestClient.put('/aps/strategy/update', data);
}

/** 删除APS-排程策略 */
export function deleteStrategy(id: number) {
  return requestClient.delete(`/aps/strategy/delete?id=${id}`);
}

/** 批量删除APS-排程策略 */
export function deleteStrategyList(ids: number[]) {
  return requestClient.delete(
    `/aps/strategy/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出APS-排程策略 */
export function exportStrategy(params: any) {
  return requestClient.download('/aps/strategy/export-excel', { params });
}

/** 下载APS-排程策略导入模板 */
export function importStrategyTemplate() {
  return requestClient.download('/aps/strategy/get-import-template');
}

/** 导入APS-排程策略 */
export function importStrategy(file: File) {
  return requestClient.upload('/aps/strategy/import', { file });
}

