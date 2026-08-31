import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

const STATUS_QUERY_FIELD = {
  field: 'status',
  label: '状态',
  type: 'select' as const,
  get options() {
    return getDictOptions(DICT_TYPE.COMMON_STATUS, 'number');
  },
};

export const WH_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '仓库编码', minWidth: 140 },
    { field: 'name', label: '仓库名称', minWidth: 160 },
    { field: 'whType', label: '仓库类型', minWidth: 120 },
  ],
  detailUri: '/wms/wh/get?id={id}',
  dialogTitle: '选择仓库',
  displayFields: ['code', 'name'],
  pageUri: '/wms/wh/page',
  placeholder: '请选择仓库',
  queryFields: [
    { field: 'code', label: '仓库编码' },
    { field: 'name', label: '仓库名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const ZONE_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '库区编码', minWidth: 140 },
    { field: 'name', label: '库区名称', minWidth: 160 },
    { field: 'whName', label: '所属仓库', minWidth: 160 },
  ],
  detailUri: '/wms/zone/get?id={id}',
  dialogTitle: '选择库区',
  displayFields: ['code', 'name'],
  pageUri: '/wms/zone/page',
  placeholder: '请选择库区',
  queryFields: [
    { field: 'code', label: '库区编码' },
    { field: 'name', label: '库区名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const LOC_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '库位编码', minWidth: 160 },
    { field: 'whName', label: '所属仓库', minWidth: 140 },
    { field: 'zoneName', label: '所属库区', minWidth: 140 },
  ],
  detailUri: '/wms/loc/get?id={id}',
  dialogTitle: '选择库位',
  displayFields: ['code'],
  pageUri: '/wms/loc/page',
  placeholder: '请选择库位',
  queryFields: [
    { field: 'code', label: '库位编码' },
    { field: 'aisleNo', label: '巷道号' },
  ],
  valueField: 'id',
};

export const CONTAINER_TYPE_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '类型编码', minWidth: 140 },
    { field: 'name', label: '类型名称', minWidth: 160 },
  ],
  detailUri: '/wms/container-type/get?id={id}',
  dialogTitle: '选择容器类型',
  displayFields: ['code', 'name'],
  pageUri: '/wms/container-type/page',
  placeholder: '请选择容器类型',
  queryFields: [
    { field: 'code', label: '类型编码' },
    { field: 'name', label: '类型名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const CONTAINER_RELATION_PROPS = {
  columns: [
    { field: 'containerNo', label: '容器编号', minWidth: 160 },
    { field: 'whName', label: '所在仓库', minWidth: 140 },
  ],
  detailUri: '/wms/container/get?id={id}',
  dialogTitle: '选择容器',
  displayFields: ['containerNo'],
  pageUri: '/wms/container/page',
  placeholder: '请选择容器',
  queryFields: [{ field: 'containerNo', label: '容器编号' }],
  valueField: 'id',
};

export const BATCH_RELATION_PROPS = {
  columns: [
    { field: 'lotNo', label: '批次号', minWidth: 160 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
  ],
  detailUri: '/wms/batch/get?id={id}',
  dialogTitle: '选择批次',
  displayFields: ['lotNo'],
  pageUri: '/wms/batch/page',
  placeholder: '请选择批次',
  queryFields: [
    { field: 'lotNo', label: '批次号' },
    { field: 'matCode', label: '物料编码' },
  ],
  valueField: 'id',
};

export const STRATEGY_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '策略编码', minWidth: 140 },
    { field: 'name', label: '策略名称', minWidth: 160 },
  ],
  detailUri: '/wms/strategy/get?id={id}',
  dialogTitle: '选择仓储策略',
  displayFields: ['code', 'name'],
  pageUri: '/wms/strategy/page',
  placeholder: '请选择仓储策略',
  queryFields: [
    { field: 'code', label: '策略编码' },
    { field: 'name', label: '策略名称' },
  ],
  valueField: 'id',
};

export const IN_ORDER_RELATION_PROPS = {
  columns: [
    { field: 'inNo', label: '入库单号', minWidth: 160 },
    { field: 'whName', label: '目标仓库', minWidth: 140 },
  ],
  detailUri: '/wms/in-order/get?id={id}',
  dialogTitle: '选择入库单',
  displayFields: ['inNo'],
  pageUri: '/wms/in-order/page',
  placeholder: '请选择入库单',
  queryFields: [{ field: 'inNo', label: '入库单号' }],
  valueField: 'id',
};

export const OUT_ORDER_RELATION_PROPS = {
  columns: [
    { field: 'outNo', label: '出库单号', minWidth: 160 },
    { field: 'whName', label: '来源仓库', minWidth: 140 },
  ],
  detailUri: '/wms/out-order/get?id={id}',
  dialogTitle: '选择出库单',
  displayFields: ['outNo'],
  pageUri: '/wms/out-order/page',
  placeholder: '请选择出库单',
  queryFields: [{ field: 'outNo', label: '出库单号' }],
  valueField: 'id',
};

/**
 * 出库明细选择器。
 *
 * <p>明细必须限定在某一张出库单下，调用方通过 {@code extraParams: { orderId }} 传入父键，
 * 避免用户从别的单据里挑明细。
 */
export const OUT_ORDER_LINE_RELATION_PROPS = {
  columns: [
    { field: 'lineNo', label: '行号', minWidth: 80 },
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'planQty', label: '计划数量', minWidth: 110 },
    { field: 'fromLocCode', label: '来源库位', minWidth: 130 },
  ],
  detailUri: '/wms/out-order/out-order-line/get?id={id}',
  dialogTitle: '选择出库明细',
  displayFields: ['lineNo', 'matCode'],
  pageUri: '/wms/out-order/out-order-line/page',
  placeholder: '请选择出库明细',
  queryFields: [
    { field: 'lotNo', label: '批次号' },
  ],
  valueField: 'id',
};

/**
 * 库存余额选择器（盘点明细用）。
 *
 * <p>盘点行必须落在具体一条余额上，账面数量与盘点维度都由它带出来。
 */
export const INV_BALANCE_LINE_RELATION_PROPS = {
  columns: [
    { field: 'matCode', label: '物料编码', minWidth: 140 },
    { field: 'matName', label: '物料名称', minWidth: 160 },
    { field: 'lotNo', label: '批次号', minWidth: 130 },
    { field: 'locCode', label: '库位编码', minWidth: 130 },
    { field: 'qtyOnhand', label: '现存数量', minWidth: 110 },
  ],
  detailUri: '/wms/inv-balance/get?id={id}',
  dialogTitle: '选择库存余额',
  displayFields: ['matCode', 'lotNo', 'locCode'],
  pageUri: '/wms/inv-balance/page',
  placeholder: '请选择库存余额',
  queryFields: [
    { field: 'matCode', label: '物料编码' },
    { field: 'lotNo', label: '批次号' },
    { field: 'locCode', label: '库位编码' },
  ],
  valueField: 'id',
};

export const ARRIVAL_RELATION_PROPS = {
  columns: [
    { field: 'arrivalNo', label: '到货单号', minWidth: 160 },
    { field: 'supplierName', label: '供应商', minWidth: 160 },
  ],
  detailUri: '/wms/arrival/get?id={id}',
  dialogTitle: '选择到货登记',
  displayFields: ['arrivalNo'],
  pageUri: '/wms/arrival/page',
  placeholder: '请选择到货登记',
  queryFields: [{ field: 'arrivalNo', label: '到货单号' }],
  valueField: 'id',
};
