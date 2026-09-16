import type { PageResult } from '@/http/types'
import { http } from '@/http/http'

/**
 * MES 派工任务与工序任务。
 *
 * 两个端点分属不同 Controller，职责也不同：
 * - `/mes/dispatch-task/*` 是「某人领了哪张单」的派工记录，**有 operatorId 筛选**，
 *   所以「我的任务」必须以它为入口。
 * - `/mes/op-task/get` 是工序任务本体，带 `routeOpId`，是 ESOP 作业指导书的入口。
 *
 * 注意 `OpTaskPageReqVO` 没有 operatorId 筛选（后端确无此字段），
 * 不要试图用工序任务列表按人筛，筛不出来。
 */

/** 派工任务（后端 DispatchTaskRespVO） */
export interface DispatchTask {
  id: number | string
  /** 派工单号 */
  dispatchNo: string
  /** 工序任务编号，点进详情要用它 */
  opTaskId: number | string
  /** 工序任务号 */
  opTaskNo?: string
  /** 工序名称 */
  opName?: string
  /** 作业工单号 */
  workOrderNo?: string
  /** 作业人员编号 */
  operatorId?: number | string
  /** 作业人员 */
  operatorName?: string
  /** 作业班组编号 */
  teamId?: number | string
  /** 作业班组 */
  teamName?: string
  /** 生产资源编号 */
  resId?: number | string
  /** 生产资源 */
  resName?: string
  /** 班次编号 */
  shiftId?: number | string
  /** 班次 */
  shiftName?: string
  /** 计划开始时间 */
  plannedStartTime?: string
  /** 计划结束时间 */
  plannedEndTime?: string
  /** 状态，取值见字典 mes_dispatch_task_status */
  status?: string
  remark?: string
}

/**
 * 派工任务分页参数。
 *
 * 后端 PageParam 是 pageNo / pageSize（不是 page），两者 @NotNull 不能省。
 * operatorId 是本任务「我的任务」的真实筛选依据。
 */
export interface DispatchTaskPageParams {
  pageNo: number
  pageSize: number
  /** 作业人员编号，取当前登录用户的 userId */
  operatorId?: number | string
  /** 状态，取值见字典 mes_dispatch_task_status */
  status?: string
  /** 派工单号 */
  dispatchNo?: string
}

/** 工序任务（后端 OpTaskRespVO，字段按移动作业场景裁剪） */
export interface OpTask {
  id: number | string
  /** 工序任务号 */
  taskNo: string
  /** 作业工单编号 */
  workOrderId?: number | string
  /** 作业工单号快照 */
  workOrderNo?: string
  /** 生产工单号 */
  prodOrderNo?: string
  /**
   * 锁定的路线工序编号。
   *
   * **这是 ESOP 作业指导书的唯一入口**——`list-by-route-op` 只认这个 id。
   * 为空说明该工序未挂载作业步骤，详情页的「查看作业指导书」按钮要置灰。
   */
  routeOpId?: number | string
  /** 标准工序编号 */
  opId?: number | string
  /** 工序编码快照 */
  opCode?: string
  /** 工序名称快照 */
  opName?: string
  /** 工序顺序号 */
  seq?: number
  /** 并行工序组 */
  parallelGroup?: string
  /** 前序工序任务编号 */
  prevTaskId?: number | string
  /** 后序工序任务编号 */
  nextTaskId?: number | string
  /** 执行模式，取值见字典 mes_op_exec_mode */
  execMode?: string
  /** 质检模式，取值见字典 mes_qc_mode */
  qcMode?: string
  /** 跳序策略，取值见字典 mes_skip_policy */
  skipPolicy?: string
  /** 是否关键工序 */
  keyFlag?: boolean
  /** 计划数量 */
  plannedQty?: number
  /** 合格完成数量 */
  completedQty?: number
  /** 报废数量 */
  scrapQty?: number
  /** 当前分配资源编号 */
  resId?: number | string
  /** 资源名称 */
  resName?: string
  /** 计划开始时间 */
  plannedStartTime?: string
  /** 计划结束时间 */
  plannedEndTime?: string
  /** 实际开始时间 */
  actualStartTime?: string
  /** 实际结束时间 */
  actualEndTime?: string
  /** 状态，取值见字典 mes_op_task_status */
  status?: string
  /** 备注 */
  remark?: string
}

/** 分页查询派工任务。operatorId 传当前登录用户即「我的任务」 */
export function getDispatchTaskPage(params: DispatchTaskPageParams) {
  return http.get<PageResult<DispatchTask>>('/mes/dispatch-task/page', params)
}

/** 查询工序任务详情 */
export function getOpTask(id: number | string) {
  return http.get<OpTask>('/mes/op-task/get', { id })
}
