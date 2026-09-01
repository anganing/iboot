import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

/**
 * MES 模块内的关联选择器配置。
 *
 * mdm、system、wms 的主数据选择器复用 `views/mdm/relation-config.ts` 与
 * `views/wms/relation-config.ts`，这里只放 MES 自己的单据，避免两边重复定义。
 *
 * 表单里的外键一律用 `RelationSelect` 让用户从单据里挑，不允许 InputNumber 手填编号。
 */

/** 生产需求选择器，生产工单按需求建单时用。 */
export const PROD_DEMAND_RELATION_PROPS = {
  columns: [
    { field: 'demandNo', label: '需求单号', minWidth: 160 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'qty', label: '需求数量', minWidth: 110 },
  ],
  detailUri: '/mes/prod-demand/get?id={id}',
  dialogTitle: '选择生产需求',
  displayFields: ['demandNo'],
  pageUri: '/mes/prod-demand/page',
  placeholder: '请选择生产需求',
  queryFields: [
    { field: 'demandNo', label: '需求单号' },
    { field: 'matCode', label: '物料编码' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_DEMAND_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 生产工单选择器，作业工单、领退补料、返工都挂在它下面。 */
export const PROD_ORDER_RELATION_PROPS = {
  columns: [
    { field: 'orderNo', label: '生产工单号', minWidth: 160 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'qty', label: '计划数量', minWidth: 110 },
  ],
  detailUri: '/mes/prod-order/get?id={id}',
  dialogTitle: '选择生产工单',
  displayFields: ['orderNo'],
  pageUri: '/mes/prod-order/page',
  placeholder: '请选择生产工单',
  queryFields: [
    { field: 'orderNo', label: '生产工单号' },
    { field: 'matCode', label: '物料编码' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_PROD_ORDER_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 作业工单选择器，工序任务与 RGV 任务用。 */
export const WORK_ORDER_RELATION_PROPS = {
  columns: [
    { field: 'workOrderNo', label: '作业工单号', minWidth: 160 },
    { field: 'prodOrderNo', label: '生产工单号', minWidth: 160 },
    { field: 'wcName', label: '工作中心', minWidth: 140 },
    { field: 'plannedQty', label: '计划数量', minWidth: 110 },
  ],
  detailUri: '/mes/work-order/get?id={id}',
  dialogTitle: '选择作业工单',
  displayFields: ['workOrderNo'],
  pageUri: '/mes/work-order/page',
  placeholder: '请选择作业工单',
  queryFields: [
    { field: 'workOrderNo', label: '作业工单号' },
    { field: 'prodOrderNo', label: '生产工单号' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_WORK_ORDER_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 工序任务选择器，报工、派工、跳序、返工、RGV 都以它为锚点。 */
export const OP_TASK_RELATION_PROPS = {
  columns: [
    { field: 'taskNo', label: '工序任务号', minWidth: 160 },
    { field: 'workOrderNo', label: '作业工单号', minWidth: 160 },
    { field: 'opCode', label: '工序编码', minWidth: 120 },
    { field: 'opName', label: '工序名称', minWidth: 140 },
    { field: 'plannedQty', label: '计划数量', minWidth: 110 },
  ],
  detailUri: '/mes/op-task/get?id={id}',
  dialogTitle: '选择工序任务',
  displayFields: ['taskNo'],
  pageUri: '/mes/op-task/page',
  placeholder: '请选择工序任务',
  queryFields: [
    { field: 'taskNo', label: '工序任务号' },
    { field: 'workOrderNo', label: '作业工单号' },
    { field: 'opCode', label: '工序编码' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_OP_TASK_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 检验方案选择器，质检任务与质检规则只允许引用已发布方案。 */
export const QC_PLAN_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '方案编码', minWidth: 140 },
    { field: 'name', label: '方案名称', minWidth: 180 },
    { field: 'versionNo', label: '版本号', minWidth: 100 },
  ],
  detailUri: '/mes/qc-plan/get?id={id}',
  dialogTitle: '选择检验方案',
  displayFields: ['code', 'name'],
  // 后端限定只能引用已发布方案，选择器提前过滤掉草稿与停用，避免选完才报错
  extraParams: { status: 'PUBLISHED' },
  pageUri: '/mes/qc-plan/page',
  placeholder: '请选择检验方案',
  queryFields: [
    { field: 'code', label: '方案编码' },
    { field: 'name', label: '方案名称' },
    { field: 'versionNo', label: '版本号' },
  ],
  valueField: 'id',
};

/** 质检任务选择器，缺陷记录与不合格品处置单用。 */
export const QC_TASK_RELATION_PROPS = {
  columns: [
    { field: 'taskNo', label: '质检任务号', minWidth: 160 },
    { field: 'bizNo', label: '来源单号', minWidth: 150 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
  ],
  detailUri: '/mes/qc-task/get?id={id}',
  dialogTitle: '选择质检任务',
  displayFields: ['taskNo'],
  pageUri: '/mes/qc-task/page',
  placeholder: '请选择质检任务',
  queryFields: [
    { field: 'taskNo', label: '质检任务号' },
    { field: 'bizNo', label: '来源单号' },
    { field: 'matCode', label: '物料编码' },
    {
      field: 'status',
      label: '状态',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_QC_TASK_STATUS, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** 质检触发规则选择器，质检任务记录命中的规则。 */
export const QC_RULE_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '规则编码', minWidth: 140 },
    { field: 'name', label: '规则名称', minWidth: 180 },
    { field: 'triggerPoint', label: '触发点', minWidth: 110 },
  ],
  detailUri: '/mes/qc-rule/get?id={id}',
  dialogTitle: '选择质检触发规则',
  displayFields: ['code', 'name'],
  pageUri: '/mes/qc-rule/page',
  placeholder: '请选择质检触发规则',
  queryFields: [
    { field: 'code', label: '规则编码' },
    { field: 'name', label: '规则名称' },
  ],
  valueField: 'id',
};

/**
 * 检验项目选择器。
 *
 * 检验项必须限定在某个检验方案下，调用方通过 `extraParams: { planId }` 传入父键，
 * 否则会挑到别的方案的检验项，结果录到哪个方案上就说不清了。
 */
export const QC_ITEM_RELATION_PROPS = {
  columns: [
    { field: 'itemCode', label: '检验项编码', minWidth: 140 },
    { field: 'itemName', label: '检验项名称', minWidth: 180 },
    { field: 'unit', label: '单位', minWidth: 90 },
    { field: 'stdValue', label: '标准值', minWidth: 120 },
  ],
  detailUri: '/mes/qc-item/get?id={id}',
  dialogTitle: '选择检验项目',
  displayFields: ['itemCode', 'itemName'],
  pageUri: '/mes/qc-item/page',
  placeholder: '请选择检验项目',
  queryFields: [
    { field: 'itemCode', label: '检验项编码' },
    { field: 'itemName', label: '检验项名称' },
  ],
  valueField: 'id',
};

/**
 * 检验结果选择器。
 *
 * 缺陷记录关联到具体检验项的判定结果时用，必须限定在同一质检任务下
 * （`extraParams: { taskId }`），后端也会校验归属。
 */
export const QC_RESULT_RELATION_PROPS = {
  columns: [
    { field: 'itemCode', label: '检验项编码', minWidth: 140 },
    { field: 'itemName', label: '检验项名称', minWidth: 180 },
    { field: 'sampleNo', label: '样本序号', minWidth: 100 },
    { field: 'actualValue', label: '实测值', minWidth: 120 },
  ],
  detailUri: '/mes/qc-result/get?id={id}',
  dialogTitle: '选择检验结果',
  displayFields: ['itemCode', 'sampleNo'],
  pageUri: '/mes/qc-result/page',
  placeholder: '请选择检验结果',
  queryFields: [
    { field: 'itemCode', label: '检验项编码' },
    { field: 'itemName', label: '检验项名称' },
  ],
  valueField: 'id',
};

/** 领退补料申请选择器，明细子表的父键用。 */
export const MATERIAL_REQ_RELATION_PROPS = {
  columns: [
    { field: 'reqNo', label: '申请单号', minWidth: 160 },
    { field: 'prodOrderNo', label: '生产工单号', minWidth: 160 },
    { field: 'whName', label: '仓库', minWidth: 140 },
  ],
  detailUri: '/mes/material-req/get?id={id}',
  dialogTitle: '选择领退补料申请',
  displayFields: ['reqNo'],
  pageUri: '/mes/material-req/page',
  placeholder: '请选择领退补料申请',
  queryFields: [
    { field: 'reqNo', label: '申请单号' },
    { field: 'prodOrderNo', label: '生产工单号' },
  ],
  valueField: 'id',
};

/** 缺陷代码选择器，缺陷记录按它落编码、名称、等级快照。 */
export const DEFECT_CODE_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '缺陷编码', minWidth: 140 },
    { field: 'name', label: '缺陷名称', minWidth: 180 },
    { field: 'level', label: '缺陷等级', minWidth: 110 },
  ],
  detailUri: '/mdm/defect-code/get?id={id}',
  dialogTitle: '选择缺陷代码',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/defect-code/page',
  placeholder: '请选择缺陷代码',
  queryFields: [
    { field: 'code', label: '缺陷编码' },
    { field: 'name', label: '缺陷名称' },
    {
      field: 'level',
      label: '缺陷等级',
      type: 'select' as const,
      get options() {
        return getDictOptions(DICT_TYPE.MES_DEFECT_LEVEL, 'string');
      },
    },
  ],
  valueField: 'id',
};

/** BOM 选择器，生产工单锁定 BOM 版本时用。 */
export const BOM_RELATION_PROPS = {
  columns: [
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'version', label: 'BOM 版本', minWidth: 110 },
    { field: 'effectiveDate', label: '生效日期', minWidth: 120 },
  ],
  detailUri: '/mdm/bom/get?id={id}',
  dialogTitle: '选择 BOM',
  displayFields: ['matCode', 'version'],
  pageUri: '/mdm/bom/page',
  placeholder: '请选择 BOM',
  queryFields: [
    { field: 'matCode', label: '物料编码' },
    { field: 'version', label: 'BOM 版本' },
  ],
  valueField: 'id',
};

/** 工艺路线选择器，生产工单与返工任务锁定路线版本时用。 */
export const ROUTE_RELATION_PROPS = {
  columns: [
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'version', label: '路线版本', minWidth: 110 },
    { field: 'effectiveDate', label: '生效日期', minWidth: 120 },
  ],
  detailUri: '/mdm/route/get?id={id}',
  dialogTitle: '选择工艺路线',
  displayFields: ['matCode', 'version'],
  pageUri: '/mdm/route/page',
  placeholder: '请选择工艺路线',
  queryFields: [
    { field: 'matCode', label: '物料编码' },
    { field: 'version', label: '路线版本' },
  ],
  valueField: 'id',
};

/**
 * 路线工序选择器。
 *
 * 工序任务锁定的是某条工艺路线下的具体工序，调用方通过 `extraParams: { routeId }`
 * 限定路线，工序编码、顺序、并行组都由后端按它带出快照。
 */
export const ROUTE_OP_RELATION_PROPS = {
  columns: [
    { field: 'seq', label: '工序顺序', minWidth: 100 },
    { field: 'opCode', label: '工序编码', minWidth: 140 },
    { field: 'opName', label: '工序名称', minWidth: 160 },
    { field: 'wcName', label: '工作中心', minWidth: 140 },
  ],
  detailUri: '/mdm/route/route-op/get?id={id}',
  dialogTitle: '选择路线工序',
  displayFields: ['opCode', 'opName'],
  pageUri: '/mdm/route/route-op/page',
  placeholder: '请选择路线工序',
  queryFields: [],
  valueField: 'id',
};
