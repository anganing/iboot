/**
 * MES 单据与工序的状态取值与动作可用条件，六个单据页与作业页共用这一份。
 *
 * 状态判断散在各页字符串字面量里，改一处漏五处就会出现"按钮能点、后端拒绝"的空转。
 * 取值与后端 `MesProdOrderStatusEnum` / `MesDemandStatusEnum` / `MesMaterialReqStatusEnum`
 * / `MesProdReportStatusEnum` / `MesOpTaskStatusEnum` / `MesOpSkipRecordStatusEnum` 逐一对齐。
 */

/** 生产工单状态，与后端 MesProdOrderStatusEnum 对齐 */
export const MES_PROD_ORDER_STATUS = {
  /** 草稿 */
  DRAFT: 'DRAFT',
  /** 已下达 */
  RELEASED: 'RELEASED',
  /** 齐套就绪 */
  READY: 'READY',
  /** 生产中 */
  RUNNING: 'RUNNING',
  /** 挂起 */
  HOLD: 'HOLD',
  /** 已完工 */
  DONE: 'DONE',
  /** 已关闭 */
  CLOSED: 'CLOSED',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 生产需求状态，与后端 MesDemandStatusEnum 对齐 */
export const MES_DEMAND_STATUS = {
  /** 草稿 */
  DRAFT: 'DRAFT',
  /** 已确认 */
  CONFIRMED: 'CONFIRMED',
  /** 已排程 */
  PLANNED: 'PLANNED',
  /** 已下达 */
  RELEASED: 'RELEASED',
  /** 已完成 */
  DONE: 'DONE',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 领退补料申请状态，与后端 MesMaterialReqStatusEnum 对齐 */
export const MES_MATERIAL_REQ_STATUS = {
  /** 草稿 */
  DRAFT: 'DRAFT',
  /** 已提交（已占预留） */
  SUBMITTED: 'SUBMITTED',
  /** WMS 处理中 */
  WMS_PROCESSING: 'WMS_PROCESSING',
  /** 部分处理 */
  PARTIAL: 'PARTIAL',
  /** 已完成 */
  DONE: 'DONE',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 报工记录状态，与后端 MesProdReportStatusEnum 对齐 */
export const MES_PROD_REPORT_STATUS = {
  /** 已提交 */
  SUBMITTED: 'SUBMITTED',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 跳过记录状态，与后端 MesOpSkipRecordStatusEnum 对齐 */
export const MES_OP_SKIP_STATUS = {
  /** 待审批 */
  PENDING: 'PENDING',
  /** 已生效 */
  EFFECTIVE: 'EFFECTIVE',
  /** 已撤销 */
  REVOKED: 'REVOKED',
} as const;

/** 工序任务状态，与后端 MesOpTaskStatusEnum 对齐 */
export const MES_OP_TASK_STATUS = {
  /** 待排产 */
  PENDING: 'PENDING',
  /** 待开工 */
  READY: 'READY',
  /** 加工中 */
  RUNNING: 'RUNNING',
  /** 已暂停 */
  PAUSED: 'PAUSED',
  /** 待质检 */
  QC_WAITING: 'QC_WAITING',
  /** 挂起 */
  HOLD: 'HOLD',
  /** 已完工 */
  DONE: 'DONE',
  /** 已跳过 */
  SKIPPED: 'SKIPPED',
  /** 已取消 */
  CANCELLED: 'CANCELLED',
} as const;

/** 生产工单是否草稿：编辑、删除、下达都只放行草稿 */
export function isProdOrderDraft(status?: string) {
  return status === MES_PROD_ORDER_STATUS.DRAFT;
}

/** 生产工单是否可挂起：生产中 */
export function canHoldProdOrder(status?: string) {
  return status === MES_PROD_ORDER_STATUS.RUNNING;
}

/** 生产工单是否可恢复：挂起 */
export function canResumeProdOrder(status?: string) {
  return status === MES_PROD_ORDER_STATUS.HOLD;
}

/** 生产工单是否可关闭：已完工（关闭是 DONE 的唯一出边，CLOSED/CANCELLED 是终态） */
export function canCloseProdOrder(status?: string) {
  return status === MES_PROD_ORDER_STATUS.DONE;
}

/** 生产工单是否可取消：草稿、已下达、齐套就绪（生产中无 cancel 出边） */
export function canCancelProdOrder(status?: string) {
  return (
    status === MES_PROD_ORDER_STATUS.DRAFT ||
    status === MES_PROD_ORDER_STATUS.RELEASED ||
    status === MES_PROD_ORDER_STATUS.READY
  );
}

/** 生产工单是否可齐套检查：已下达、齐套就绪、生产中（在制复检） */
export function canCheckKitProdOrder(status?: string) {
  return (
    status === MES_PROD_ORDER_STATUS.READY ||
    status === MES_PROD_ORDER_STATUS.RELEASED ||
    status === MES_PROD_ORDER_STATUS.RUNNING
  );
}
