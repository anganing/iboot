import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { MDM_RES_TYPE_OPTIONS } from './constants';

const STATUS_QUERY_FIELD = {
  field: 'status',
  label: '状态',
  type: 'select' as const,
  get options() {
    return getDictOptions(DICT_TYPE.COMMON_STATUS, 'number');
  },
};

export const INV_ORG_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '库存组织编码', minWidth: 140 },
    { field: 'name', label: '库存组织名称', minWidth: 160 },
  ],
  detailUri: '/mdm/inv-org/get?id={id}',
  dialogTitle: '选择库存组织',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/inv-org/page',
  placeholder: '请选择库存组织',
  queryFields: [
    { field: 'code', label: '库存组织编码' },
    { field: 'name', label: '库存组织名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const PLANT_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '工厂编码', minWidth: 140 },
    { field: 'name', label: '工厂名称', minWidth: 160 },
    { field: 'address', label: '地址', minWidth: 180 },
  ],
  detailUri: '/mdm/plant/get?id={id}',
  dialogTitle: '选择工厂',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/plant/page',
  placeholder: '请选择工厂',
  queryFields: [
    { field: 'code', label: '工厂编码' },
    { field: 'name', label: '工厂名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const WORKSHOP_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '车间编码', minWidth: 140 },
    { field: 'name', label: '车间名称', minWidth: 160 },
  ],
  detailUri: '/mdm/workshop/get?id={id}',
  dialogTitle: '选择车间',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/workshop/page',
  placeholder: '请选择车间',
  queryFields: [
    { field: 'code', label: '车间编码' },
    { field: 'name', label: '车间名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const WC_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '工作中心编码', minWidth: 140 },
    { field: 'name', label: '工作中心名称', minWidth: 160 },
  ],
  detailUri: '/mdm/wc/get?id={id}',
  dialogTitle: '选择工作中心',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/wc/page',
  placeholder: '请选择工作中心',
  queryFields: [
    { field: 'code', label: '工作中心编码' },
    { field: 'name', label: '工作中心名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const RES_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '资源编码', minWidth: 140 },
    { field: 'name', label: '资源名称', minWidth: 160 },
    { field: 'resType', label: '资源类型', minWidth: 120 },
  ],
  detailUri: '/mdm/res/get?id={id}',
  dialogTitle: '选择生产资源',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/res/page',
  placeholder: '请选择生产资源',
  queryFields: [
    { field: 'code', label: '资源编码' },
    { field: 'name', label: '资源名称' },
    {
      field: 'resType',
      label: '资源类型',
      options: MDM_RES_TYPE_OPTIONS,
      type: 'select' as const,
    },
  ],
  valueField: 'id',
};

export const PROJECT_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '项目编码', minWidth: 140 },
    { field: 'name', label: '项目名称', minWidth: 160 },
  ],
  detailUri: '/mdm/project/get?id={id}',
  dialogTitle: '选择项目',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/project/page',
  placeholder: '请选择项目',
  queryFields: [
    { field: 'code', label: '项目编码' },
    { field: 'name', label: '项目名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const PROJECT_TASK_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '任务编码', minWidth: 140 },
    { field: 'name', label: '任务名称', minWidth: 160 },
    { field: 'projectCode', label: '所属项目', minWidth: 140 },
  ],
  detailUri: '/mdm/project-task/get?id={id}',
  dialogTitle: '选择项目任务',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/project-task/page',
  placeholder: '请选择项目任务',
  queryFields: [
    { field: 'code', label: '任务编码' },
    { field: 'name', label: '任务名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const SUPPLIER_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '供应商编码', minWidth: 140 },
    { field: 'name', label: '供应商名称', minWidth: 180 },
  ],
  detailUri: '/mdm/supplier/get?id={id}',
  dialogTitle: '选择供应商',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/supplier/page',
  placeholder: '请选择供应商',
  queryFields: [
    { field: 'code', label: '供应商编码' },
    { field: 'name', label: '供应商名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const CUSTOMER_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '客户编码', minWidth: 140 },
    { field: 'name', label: '客户名称', minWidth: 180 },
  ],
  detailUri: '/mdm/customer/get?id={id}',
  dialogTitle: '选择客户',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/customer/page',
  placeholder: '请选择客户',
  queryFields: [
    { field: 'code', label: '客户编码' },
    { field: 'name', label: '客户名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const MAT_CAT_RELATION_PROPS = {
  allUri: '/mdm/mat-cat/list',
  columns: [
    { field: 'code', label: '分类编码', minWidth: 140 },
    { field: 'name', label: '分类名称', minWidth: 160 },
  ],
  detailUri: '/mdm/mat-cat/get?id={id}',
  dialogTitle: '选择物料分类',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/mat-cat/list',
  placeholder: '请选择物料分类',
  queryAll: true,
  queryFields: [
    { field: 'code', label: '分类编码' },
    { field: 'name', label: '分类名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const MAT_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '物料编码', minWidth: 140 },
    { field: 'name', label: '物料名称', minWidth: 160 },
    { field: 'spec', label: '规格型号', minWidth: 160 },
    { field: 'drawingNo', label: '图号', minWidth: 140 },
  ],
  detailUri: '/mdm/mat/get?id={id}',
  dialogTitle: '选择物料',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/mat/page',
  placeholder: '请选择物料',
  queryFields: [
    { field: 'code', label: '物料编码' },
    { field: 'name', label: '物料名称' },
    { field: 'spec', label: '规格型号' },
  ],
  valueField: 'id',
};

export const UOM_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '单位编码', minWidth: 140 },
    { field: 'name', label: '单位名称', minWidth: 140 },
  ],
  detailUri: '/mdm/uom/get?id={id}',
  dialogTitle: '选择计量单位',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/uom/page',
  placeholder: '请选择计量单位',
  queryFields: [
    { field: 'code', label: '单位编码' },
    { field: 'name', label: '单位名称' },
  ],
  valueField: 'id',
};

export const OP_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '工序编码', minWidth: 140 },
    { field: 'name', label: '工序名称', minWidth: 160 },
    { field: 'stdDuration', label: '标准时长（分钟）', minWidth: 140 },
  ],
  detailUri: '/mdm/op/get?id={id}',
  dialogTitle: '选择标准工序',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/op/page',
  placeholder: '请选择标准工序',
  queryFields: [
    { field: 'code', label: '工序编码' },
    { field: 'name', label: '工序名称' },
  ],
  valueField: 'id',
};

export const SHIFT_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '班次编码', minWidth: 140 },
    { field: 'name', label: '班次名称', minWidth: 160 },
    { field: 'startTime', label: '开始时间', minWidth: 120 },
    { field: 'endTime', label: '结束时间', minWidth: 120 },
  ],
  detailUri: '/mdm/shift/get?id={id}',
  dialogTitle: '选择班次',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/shift/page',
  placeholder: '请选择班次',
  queryFields: [
    { field: 'code', label: '班次编码' },
    { field: 'name', label: '班次名称' },
  ],
  valueField: 'id',
};

export const USER_RELATION_PROPS = {
  columns: [
    { field: 'username', label: '用户账号', minWidth: 140 },
    { field: 'nickname', label: '用户昵称', minWidth: 140 },
    { field: 'mobile', label: '手机号', minWidth: 140 },
  ],
  detailUri: '/system/user/get?id={id}',
  dialogTitle: '选择用户',
  displayFields: ['nickname'],
  pageUri: '/system/user/page',
  placeholder: '请选择用户',
  queryFields: [
    { field: 'username', label: '用户账号' },
    { field: 'nickname', label: '用户昵称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const TOOL_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '工装编码', minWidth: 140 },
    { field: 'name', label: '工装名称', minWidth: 160 },
    { field: 'toolType', label: '工装类型', minWidth: 120 },
  ],
  detailUri: '/mdm/tool/get?id={id}',
  dialogTitle: '选择工装',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/tool/page',
  placeholder: '请选择工装',
  queryFields: [
    { field: 'code', label: '工装编码' },
    { field: 'name', label: '工装名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const CALENDAR_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '日历编码', minWidth: 140 },
    { field: 'name', label: '日历名称', minWidth: 160 },
  ],
  detailUri: '/mdm/calendar/get?id={id}',
  dialogTitle: '选择日历模板',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/calendar/page',
  placeholder: '请选择日历模板',
  queryFields: [
    { field: 'code', label: '日历编码' },
    { field: 'name', label: '日历名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};

export const SKILL_RELATION_PROPS = {
  columns: [
    { field: 'code', label: '技能编码', minWidth: 140 },
    { field: 'name', label: '技能名称', minWidth: 160 },
  ],
  detailUri: '/mdm/skill/get?id={id}',
  dialogTitle: '选择技能',
  displayFields: ['code', 'name'],
  pageUri: '/mdm/skill/page',
  placeholder: '请选择技能',
  queryFields: [
    { field: 'code', label: '技能编码' },
    { field: 'name', label: '技能名称' },
    STATUS_QUERY_FIELD,
  ],
  valueField: 'id',
};
