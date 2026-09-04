import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace WmsStrategyApi {
  /** 一条筛选条件 */
  export interface Condition {
    field?: string;
    /** 算子，取值见字段元数据里该字段的 operators */
    op?: string;
    /** 比较值：算子为 IN/NOT_IN 时是数组，否则是单值 */
    value?: any;
  }

  /** 一条排序规则 */
  export interface Sort {
    field?: string;
    /** ASC 升序、DESC 降序 */
    order?: string;
  }

  /** 策略配置，对应 wms_strategy.config_json */
  export interface StrategyConfig {
    conditions?: Condition[];
    sorts?: Sort[];
    /** 上架专属：尺寸校验模式，留空表示按库位自身设置 */
    dimensionCheck?: string;
    /** 上架专属：是否允许混批上架 */
    allowMixedLot?: boolean;
    /** 补货专属：来源仓库编码 */
    fromWhCode?: string;
    /** 补货专属：单次补货量 */
    replenishQty?: number;
  }

  /** 算子元数据 */
  export interface Operator {
    op: string;
    opName: string;
    /** 值是否为多个，决定值控件用单选还是多选 */
    multiValue: boolean;
  }

  /** 可筛字段元数据 */
  export interface ConditionField {
    field: string;
    displayName: string;
    valueType: 'BOOLEAN' | 'ENUM' | 'NUMBER' | 'TEXT';
    /** 枚举字段的字典编码，非枚举字段为空 */
    dictType?: string;
    operators: Operator[];
    description?: string;
  }

  /** 可排序字段元数据 */
  export interface SortField {
    field: string;
    displayName: string;
    description?: string;
  }

  /** 一种策略类型的可配项 */
  export interface StrategyTypeMetadata {
    strategyType: string;
    typeName: string;
    /** 条件作用在哪批数据上 */
    candidateTarget?: string;
    conditionFields: ConditionField[];
    /** 为空表示该类型不支持排序 */
    sortFields: SortField[];
    defaultConfig: StrategyConfig;
  }

  /** 通用键值选项 */
  export interface Option {
    value: string;
    label: string;
  }

  /** 字段元数据，页面上有哪些字段可选、默认配置长什么样都取自它 */
  export interface FieldMetadataResult {
    types: StrategyTypeMetadata[];
    sortOrders: Option[];
  }

  /** 仓储策略信息 */
  export interface Strategy {
    id: number; // 主键
    code?: string; // 策略编码
    name?: string; // 策略名称
    strategyType?: string; // 策略类型
    whId?: number; // 适用仓库编号，0表示全局
    versionNo?: string; // 策略版本号
    configJson?: string; // 库里的原始配置内容，只读
    config?: StrategyConfig; // 解析后的策略配置
    configInvalid?: boolean; // 原始配置是否无法识别
    configInvalidReason?: string; // 原始配置无法识别的原因
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

/** 查询策略字段元数据，供配置编辑器渲染可选字段与默认值 */
export function getFieldMetadata() {
  return requestClient.get<WmsStrategyApi.FieldMetadataResult>(
    '/wms/strategy/field-metadata',
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


