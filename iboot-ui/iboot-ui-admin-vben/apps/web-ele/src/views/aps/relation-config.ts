import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/**
 * APS 模块内的关联选择器配置。
 *
 * <p>表单里的外键一律用 RelationSelect 从主数据里挑，不让用户手填主键编号。
 * 跨模块的主数据（工厂、车间、物料、资源、项目）复用 views/mdm/relation-config.ts，这里只放 aps 自己的对象。
 */

/** 排程策略选择器：场景引用策略时用。 */
export const APS_STRATEGY_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '策略编码', minWidth: 140 },
    { field: 'name', label: '策略名称', minWidth: 160 },
    { field: 'versionNo', label: '版本号', minWidth: 100 },
  ],
  detailUri: '/aps/strategy/get?id={id}',
  dialogTitle: '选择排程策略',
  displayFields: ['code', 'name'],
  pageUri: '/aps/strategy/page',
  placeholder: '请选择排程策略',
  queryFields: [
    { field: 'code', label: '策略编码' },
    { field: 'name', label: '策略名称' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.APS_STRATEGY_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 排程场景选择器：投影与版本按场景过滤时用。 */
export const APS_SCENARIO_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '场景编码', minWidth: 140 },
    { field: 'name', label: '场景名称', minWidth: 160 },
    { field: 'plantName', label: '工厂', minWidth: 130 },
  ],
  detailUri: '/aps/scenario/get?id={id}',
  dialogTitle: '选择排程场景',
  displayFields: ['code', 'name'],
  pageUri: '/aps/scenario/page',
  placeholder: '请选择排程场景',
  queryFields: [
    { field: 'code', label: '场景编码' },
    { field: 'name', label: '场景名称' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.APS_SCENARIO_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 排程版本选择器：任务、依赖、缺料、负荷、快照都挂在版本下。 */
export const APS_VERSION_RELATION_PROPS = {
  columns: [
    { field: 'scenarioCode', label: '场景编码', minWidth: 140 },
    { field: 'scenarioName', label: '场景名称', minWidth: 150 },
    { field: 'versionNo', label: '版本号', minWidth: 90 },
    { field: 'hardScore', label: '硬约束得分', minWidth: 110 },
    { field: 'softScore', label: '软约束得分', minWidth: 110 },
  ],
  detailUri: '/aps/version/get?id={id}',
  dialogTitle: '选择排程版本',
  displayFields: ['scenarioCode', 'versionNo'],
  pageUri: '/aps/version/page',
  placeholder: '请选择排程版本',
  queryFields: [
    { field: 'versionNo', label: '版本号' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.APS_VERSION_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 排程任务选择器：任务依赖的前置与后置任务从这里挑。 */
export const APS_TASK_RELATION_PROPS = {
  columns: [
    { field: 'taskNo', label: '任务编号', minWidth: 150 },
    { field: 'prodOrderNo', label: '生产工单号', minWidth: 140 },
    { field: 'opCode', label: '工序编码', minWidth: 110 },
    { field: 'opName', label: '工序名称', minWidth: 130 },
    { field: 'resCode', label: '资源编码', minWidth: 110 },
  ],
  detailUri: '/aps/task/get?id={id}',
  dialogTitle: '选择排程任务',
  displayFields: ['taskNo', 'opName'],
  pageUri: '/aps/task/page',
  placeholder: '请选择排程任务',
  queryFields: [
    { field: 'taskNo', label: '任务编号' },
    { field: 'prodOrderNo', label: '生产工单号' },
    { field: 'opCode', label: '工序编码' },
  ],
  valueField: 'id',
};

/** 生产工单选择器：缺料筛选使用工单号，不让用户手填内部编号。 */
export const MES_PROD_ORDER_RELATION_PROPS = {
  columns: [
    { field: 'orderNo', label: '生产工单号', minWidth: 160 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'dueDate', label: '交期', minWidth: 160 },
  ],
  detailUri: '/mes/prod-order/get?id={id}',
  dialogTitle: '选择生产工单',
  displayFields: ['orderNo', 'matCode'],
  pageUri: '/mes/prod-order/page',
  placeholder: '请选择生产工单',
  queryFields: [
    { field: 'orderNo', label: '生产工单号' },
    { field: 'matCode', label: '物料编码' },
    { field: 'matName', label: '物料名称' },
  ],
  valueField: 'id',
};
