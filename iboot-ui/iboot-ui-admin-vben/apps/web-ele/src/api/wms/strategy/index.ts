import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsStrategyApi {
  /** 仓储策略信息 */
  export interface Strategy {
    id: number; // 主键
    code?: string; // 策略编码
    name?: string; // 策略名称
    strategyType?: string; // 策略类型
    whId?: number; // 适用仓库编号，0表示全局
    versionNo?: string; // 策略版本号
    configJson?: string; // 策略条件与排序规则配置
    priority?: number; // 优先级，数值越大越优先
    status?: string; // 状态
    remark?: string; // 备注
    createTime?: string; // 创建时间
    whCode?: string; // 适用仓库编号，0表示全局编码
    whName?: string; // 适用仓库编号，0表示全局名称
  }
}

/** 查询仓储策略分页 */
export function getStrategyPage(params: PageParam) {
  return requestClient.get<PageResult<WmsStrategyApi.Strategy>>(
    '/wms/strategy/page',
    { params },
  );
}

/** 查询仓储策略详情 */
export function getStrategy(id: number) {
  return requestClient.get<WmsStrategyApi.Strategy>(
    `/wms/strategy/get?id=${id}`,
  );
}

/** 新增仓储策略 */
export function createStrategy(data: WmsStrategyApi.Strategy) {
  return requestClient.post('/wms/strategy/create', data);
}

/** 修改仓储策略 */
export function updateStrategy(data: WmsStrategyApi.Strategy) {
  return requestClient.put('/wms/strategy/update', data);
}

/** 删除仓储策略 */
export function deleteStrategy(id: number) {
  return requestClient.delete(`/wms/strategy/delete?id=${id}`);
}

/** 批量删除仓储策略 */
export function deleteStrategyList(ids: number[]) {
  return requestClient.delete(
    `/wms/strategy/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出仓储策略 */
export function exportStrategy(params: any) {
  return requestClient.download('/wms/strategy/export-excel', { params });
}

/** 下载仓储策略导入模板 */
export function importStrategyTemplate() {
  return requestClient.download('/wms/strategy/get-import-template');
}

/** 导入仓储策略 */
export function importStrategy(file: File) {
  return requestClient.upload('/wms/strategy/import', { file });
}


