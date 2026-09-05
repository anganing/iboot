/**
 * WMS 单据页面的公共约定：状态取值、动作可用条件、创建幂等键、明细行号与本地行标识。
 *
 * 入库、出库、移库调拨、盘点、到货五个单据页共用这一份，避免每页各写一套状态字符串——
 * 状态判断散在五个文件里，改一处漏四处就会出现"按钮能点、后端拒绝"的空转。
 * 取值与后端 `WmsOrderStatusEnum`、`WmsArrivalStatusEnum` 逐一对齐。
 */

/** 单据状态，与后端 WmsOrderStatusEnum 对齐（入库、出库、调拨、盘点共用一套状态码） */
export const WMS_ORDER_STATUS = {
  /** 新建 */
  DRAFT: 'DRAFT',
  /** 已确认 */
  SUBMITTED: 'SUBMITTED',
  /** 待质检 */
  QC_PENDING: 'QC_PENDING',
  /** 已审批 */
  APPROVED: 'APPROVED',
  /** 已冻结账面 */
  FROZEN: 'FROZEN',
  /** 待作业 */
  READY: 'READY',
  /** 任务已创建 */
  TASK_CREATED: 'TASK_CREATED',
  /** 作业中 */
  IN_PROGRESS: 'IN_PROGRESS',
  /** 盘点中 */
  COUNTING: 'COUNTING',
  /** 差异已确认 */
  DIFF_CONFIRMED: 'DIFF_CONFIRMED',
  /** 已过账 */
  DONE: 'DONE',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
  /** 异常 */
  EXCEPTION: 'EXCEPTION',
} as const;

/** 到货登记状态，与后端 WmsArrivalStatusEnum 对齐 */
export const WMS_ARRIVAL_STATUS = {
  /** 草稿 */
  DRAFT: 'DRAFT',
  /** 已到货 */
  ARRIVED: 'ARRIVED',
  /** 待质检 */
  QC_PENDING: 'QC_PENDING',
  /** 已完成 */
  DONE: 'DONE',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 到货明细状态，与后端 WmsArrivalLineStatusEnum 对齐 */
export const WMS_ARRIVAL_LINE_STATUS = {
  /** 待处理，确认到货前的初始状态 */
  PENDING: 'PENDING',
  /** 待质检 */
  QC_PENDING: 'QC_PENDING',
  /** 待入库 */
  READY: 'READY',
  /** 已完成，已生成入库单 */
  DONE: 'DONE',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 盘点明细状态，与后端 WmsCountLineStatusEnum 对齐（字典 wms_count_line_status） */
export const WMS_COUNT_LINE_STATUS = {
  /** 待盘点，行刚按余额快照生成 */
  PENDING: 'PENDING',
  /** 已盘点，实盘等于账面 */
  COUNTED: 'COUNTED',
  /** 有差异，实盘不等于账面，等确认差异 */
  DIFF: 'DIFF',
  /** 差异已过账，已生成盘盈入库单或盘亏出库单 */
  ADJUSTED: 'ADJUSTED',
} as const;

/**
 * 是否新建态。
 *
 * 编辑、删除、提交、取消四个动作后端都只放行新建态（`requireDraft` 与 `isDraft`），
 * 非新建态一律禁用按钮而不是隐藏，用户才知道"为什么不能点"。
 */
export function isOrderDraft(status?: string) {
  return status === WMS_ORDER_STATUS.DRAFT;
}

/**
 * 是否可过账：新建或已确认。
 *
 * 与后端 `WmsOrderStatusEnum.canPost` 一致——已确认的单据可以直接过账，
 * 已过账、已取消的不行。
 */
export function canPostOrder(status?: string) {
  return (
    status === WMS_ORDER_STATUS.DRAFT || status === WMS_ORDER_STATUS.SUBMITTED
  );
}

/** 盘点单可录实盘的状态：冻结后录是常规路径，不冻结直接盘与继续补录也放行 */
export function canSubmitCountResult(status?: string) {
  return (
    status === WMS_ORDER_STATUS.DRAFT ||
    status === WMS_ORDER_STATUS.FROZEN ||
    status === WMS_ORDER_STATUS.COUNTING
  );
}

/** 盘点单可确认差异的状态：录过实盘之后 */
export function canConfirmCountDiff(status?: string) {
  return (
    status === WMS_ORDER_STATUS.COUNTING ||
    status === WMS_ORDER_STATUS.DIFF_CONFIRMED
  );
}

/**
 * 盘点行是否有差异。
 *
 * `diffQty` 是服务端在提交实盘时算出来的（实盘 - 账面），行状态随之置 `DIFF`；
 * 界面按状态判断而不是自己比数量，避免盲盘单据上界面拿不到账面数量时判断口径不一致。
 */
export function isCountLineDiff(status?: string) {
  return status === WMS_COUNT_LINE_STATUS.DIFF;
}

/** 到货登记是否草稿态：编辑、删除、确认到货都只放行草稿 */
export function isArrivalDraft(status?: string) {
  return status === WMS_ARRIVAL_STATUS.DRAFT;
}

/** 到货登记是否可生成入库单：确认到货之后才有待入库明细 */
export function canCreateInOrderFromArrival(status?: string) {
  return (
    status === WMS_ARRIVAL_STATUS.ARRIVED ||
    status === WMS_ARRIVAL_STATUS.QC_PENDING
  );
}

/**
 * 到货明细是否可生成入库单。
 *
 * 只有"待入库"的明细能生成：待质检的要等 IQC 结论，待处理的还没注册批次。
 * 后端 `selectReadyLines` 对非待入库的明细直接抛错，所以按钮按这个条件禁用而不是让用户撞错误提示。
 */
export function isArrivalLineReady(status?: string) {
  return status === WMS_ARRIVAL_LINE_STATUS.READY;
}

/** 关联选择器回填快照的回调：把选中行交给调用方，用来取物料编码、库位编码这类可读字段 */
export type RelationPickHandler = (
  field: string,
  row?: Record<string, any>,
) => void;

/**
 * 生成一次表单会话内稳定的创建幂等键。
 *
 * 弹窗打开时生成一次，提交失败重试时复用同一个键：`wms_xxx_order.idempotency_key` 上有唯一索引，
 * 后端 `createXxx` 开头按它判重并返回原单号。每次点提交都换一个新键，等于同一张单被建两次。
 */
export function newCreateIdempotencyKey(resource: string) {
  const random = Math.random().toString(36).slice(2, 10);
  return `UI:${resource}:CREATE:${Date.now()}${random}`;
}

/** 明细行的本地标识字段名。界面新建的行没有主键，vxe 需要一个稳定的 rowKey 才能定位行 */
export const LOCAL_LINE_KEY = '_localKey';

/** 带本地标识的明细行：`_localKey` 只在前端存在，提交前用 stripLocalKey 去掉 */
export type LocalLineRow<T> = T & { _localKey?: string };

let localLineSeq = 0;

/** 取下一个本地行标识 */
export function nextLocalLineKey() {
  localLineSeq += 1;
  return `local-${localLineSeq}`;
}

/**
 * 按当前顺序重排行号，步长 10。
 *
 * 行号不是装饰：过账时每行的库存流水幂等键是 `{单据键}:POST:{行号}`，两行撞同一个行号，
 * 第二行会被当成重复过账静默跳过，货就少扣了一份。所以行号由前端统一重排、不让用户填。
 */
export function renumberLines<T extends { lineNo?: number }>(lines: T[]) {
  return lines.map((line, index) => ({ ...line, lineNo: (index + 1) * 10 }));
}

/**
 * 可选外键留空时补 0。
 *
 * 单据表上"项目编号""来源仓库编号"这类列是 `NOT NULL DEFAULT 0`、请求 VO 上又是 `@NotNull`，
 * 0 表示"无项目""非调拨"。表单里留空拿到的是 undefined，直接提交会被后端校验挡回来。
 */
export function zeroIfBlank(value: unknown) {
  return value === undefined || value === null || value === '' ? 0 : value;
}

/** 去掉本地行标识，得到可以提交给后端的明细行 */
export function stripLocalKey(line: Record<string, any>) {
  const rest: Record<string, any> = { ...line };
  delete rest[LOCAL_LINE_KEY];
  return rest;
}
