import type { PageResult } from '@/http/types'
import { http } from '@/http/http'

/**
 * WMS 多维库存余额。
 *
 * 出参已带可读快照（matCode/matName/matSpec/whCode/whName/locCode/projectName），
 * 前端不需要再补关联查询。
 *
 * 注意：后端没有 qtyAvailable 字段，可用数量由前端计算，见 utils.ts 的 availableQty。
 */
export interface InvBalance {
  id: number
  /** 物料编码快照 */
  matCode: string
  /** 物料名称快照 */
  matName: string
  /** 物料规格快照 */
  matSpec?: string
  /** 基准单位编码快照 */
  uomCode?: string
  /** 批次号快照 */
  lotNo?: string
  /** 仓库编号 */
  whId?: number
  /** 仓库编码快照 */
  whCode?: string
  /** 仓库名称 */
  whName?: string
  /** 库位编号 */
  locId?: number
  /** 库位编码快照 */
  locCode?: string
  /** 归属项目名称，空表示无项目归属 */
  projectName?: string
  /** 现存数量，基准单位 */
  qtyOnhand: number
  /** 预留数量，基准单位 */
  qtyReserved: number
  /** 冻结数量，基准单位 */
  qtyFrozen: number
  /** 质量状态，取值见字典 wms_quality_status */
  qualityStatus?: string
  /** 库存范围，取值见字典 wms_stock_scope */
  stockScope?: string
  /** 乐观锁版本 */
  version?: number
  remark?: string
  /** 创建时间 */
  createTime?: string
}

/**
 * 分页查询参数。
 *
 * 后端 PageParam 是 pageNo / pageSize（不是 page），且两者都标了 @NotNull，不能省略。
 */
export interface InvBalancePageParams {
  pageNo: number
  pageSize: number
  matCode?: string
  lotNo?: string
  whId?: number
  qualityStatus?: string
  stockScope?: string
}

/** 分页查询库存余额 */
export function getInvBalancePage(params: InvBalancePageParams) {
  return http.get<PageResult<InvBalance>>('/wms/inv-balance/page', params)
}

/** 查询库存余额详情 */
export function getInvBalance(id: number) {
  return http.get<InvBalance>('/wms/inv-balance/get', { id })
}
