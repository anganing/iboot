import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace ApsStrategyApi {
  /** 单条约束的硬软权重 */
  export interface ConstraintWeight {
    hard: number; // 硬约束权重
    soft: number; // 软约束权重
  }

  /** 求解与约束权重配置，对应 aps_strategy.config_json */
  export interface StrategyConfig {
    spentLimitSeconds: number; // 求解最长耗时（秒）
    unimprovedSpentLimitSeconds: number; // 多久没有更优解就停（秒）
    materialConstrained: boolean; // 是否约束物料齐套
    weights: Record<string, ConstraintWeight>; // 约束权重，键为约束名
  }

  /** 单条约束的元数据 */
  export interface ConstraintMetadata {
    constraintName: string; // 约束名
    displayName: string; // 约束中文名
    category: string; // 约束类别：HARD、SOFT
    categoryName: string; // 约束类别中文名
    description: string; // 业务说明：罚什么、权重调大调小会怎样
    defaultHard: number; // 默认硬约束权重
    defaultSoft: number; // 默认软约束权重
  }

  /** 约束元数据，页面上有哪些约束可调、默认调到多少都取自它 */
  export interface ConstraintMetadataResult {
    constraints: ConstraintMetadata[]; // 约束清单，顺序即展示顺序
    defaultConfig: StrategyConfig; // 策略级默认配置
  }

  /** APS-排程策略信息 */
  export interface Strategy {
    id?: number; // 主键
    code?: string; // 排程策略编码，留空由编码规则分配
    name?: string; // 排程策略名称
    versionNo?: string; // 策略版本号
    objective?: string; // 优化目标说明
    configJson?: string; // 库里的原始配置内容，只读
    config?: StrategyConfig; // 解析后的求解与约束权重配置
    configInvalid?: boolean; // 原始配置是否无法识别
    configInvalidReason?: string; // 原始配置无法识别的原因
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

/** 查询约束元数据，供策略配置表单渲染可调项与默认值 */
export function getConstraintMetadata() {
  return requestClient.get<ApsStrategyApi.ConstraintMetadataResult>(
    '/aps/strategy/constraint-metadata',
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
