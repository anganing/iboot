import { httpPost } from '@/http/http'

/**
 * MES 生产报工（提交即生效，后端没有草稿态）。
 *
 * ## 为什么这个文件里要自己格式化时间
 *
 * 后端 `startTime` / `endTime` / `reportTime` 是 `LocalDateTime`，Spring 侧靠
 * `@DateTimeFormat(pattern = 'yyyy-MM-dd HH:mm:ss')` 接收。`Date.prototype.toISOString()`
 * 产出的是 `2026-09-16T22:30:00.000Z`，带 `T` 与 `Z`，**解析直接失败**，
 * 而且失败信息不会告诉你是时间格式问题。所以必须自己按 `yyyy-MM-dd HH:mm:ss` 拼。
 *
 * 注意这是**本地时间**字符串，不做时区换算：后端按服务器本地时区解释它，
 * 而现场作业时间本来就是"墙上挂钟显示的时间"，不是 UTC 时刻。
 *
 * ## 幂等键
 *
 * `idempotencyKey` 在 `ProdReportSubmitReqVO` 上是 `@NotBlank`，且**服务端不派生**——
 * 这是所有 MES/WMS 动作端点里唯一的例外（其余端点留空由服务端按四段格式派生）。
 * 键落在 `mes_prod_report.idempotency_key`，由唯一索引 `uk_mes_prod_report_idem_deleted` 兜住。
 *
 * 格式 `APP:{工序任务号}:REPORT:{seq}`，`seq` 是"这道工序的第几次报工"，
 * **只有调用方知道**——服务端按已有报工数派生的话，每次重试都会算出一个新 `seq`，
 * 每次都建一条新记录，幂等当场失效。
 *
 * 生成与沿用由页面 `pages/mes/task/report.vue` 负责：打开页面时生成一次并存在 `ref` 里，
 * 提交失败重试不变，成功后才重新生成。本文件只负责把键透传出去。
 */

/**
 * 报工提交参数。
 *
 * ⚠️ **下面这几个"必填"与 `dev-docs/移动端接口契约.md` 的表格不一致，以本注释为准。**
 * 契约里 `workerId` 写的是"留空落 0"、三个数量写的是"否"，那是 BO 层的宽容描述；
 * 库表 `mes_prod_report` 的 `worker_id` 与作业时间三列都是 `NOT NULL`，业务上也不允许
 * 一条没有作业人员、没有数量的报工。页面侧一律传真实值：
 * `workerId` 取登录用户、三个数量默认给 0（**不能是空**）、起止时间默认给实际值。
 */
export interface ProdReportSubmitParams {
  /** 工序任务编号，必填 */
  opTaskId: number
  /** 报工作业人员编号，必填，取当前登录用户的 userId */
  workerId: number
  /** 本次合格数，必填，可为零不可为负 */
  qtyGood: number
  /** 本次报废数，必填，可为零不可为负 */
  qtyScrap: number
  /** 本次返工数，必填，只记录不进三级数量推进 */
  qtyRework: number
  /** 本次作业开始时间，必填，格式 `YYYY-MM-DD HH:mm:ss` */
  startTime: string
  /** 本次作业结束时间，必填，格式 `YYYY-MM-DD HH:mm:ss`，不能早于开始时间 */
  endTime: string
  /** 幂等键，必填，格式 `APP:{工序任务号}:REPORT:{seq}` */
  idempotencyKey: string
  /** 生产资源编号，不传由后端落 0 */
  resId?: number
  /** 班次编号，不传由后端落 0 */
  shiftId?: number
  /** 备注，最长 500 */
  remark?: string
}

/**
 * 提交报工，创建即提交，并把数量推进到工序任务 + 作业工单 + 生产工单三级。
 *
 * 只有 `status = RUNNING`（加工中）的工序任务能报工，否则后端抛
 * `OP_TASK_STATUS_INVALID` `1_064_002_000`。返工数不参与"累计不超计划"那个等式。
 *
 * @returns 报工记录 id。**幂等命中时返回的是首次那条的 id**，不是新记录。
 */
export function createProdReport(params: ProdReportSubmitParams) {
  return httpPost<number>('/mes/prod-report/create', params)
}
