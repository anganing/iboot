import type { TagType } from '@wot-ui/ui/components/wd-tag/types'
import type { InvBalance } from '@/api/wms/invbalance'

/**
 * 数量归一化展示。
 *
 * 后端 qtyOnhand / qtyReserved / qtyFrozen 是 BigDecimal 序列化的数字，
 * 直接插值会渲染成 100.0000 这种带小数尾巴的形式，这里统一收敛。
 * 非数字（null / undefined / 空串）按 0 处理。
 */
export function formatQty(value?: number | string | null): string {
  const num = Number(value ?? 0)
  if (Number.isNaN(num))
    return '0'
  // 先按基准单位精度归一，再交给 Number 去掉多余的 0
  return String(Number(num.toFixed(3)))
}

/**
 * 可用数量 = 现存 - 预留 - 冻结。
 *
 * 后端 InvBalanceRespVO 只有 qtyOnhand / qtyReserved / qtyFrozen 三个字段，
 * 没有 qtyAvailable，所以可用量只能前端算。
 * 算法与 PC 端 web-ele/src/views/wms/invbalance/data.ts 的「可用数量」列 formatter 保持一致，
 * 不要另起一套。现场查料真正要回答的是「这个料我还能领多少」，只看现存会把
 * 已被预留、已被冻结的部分算进来，导致误判可领量。
 */
export function availableQty(item: Pick<InvBalance, 'qtyOnhand' | 'qtyReserved' | 'qtyFrozen'>): number {
  return Number(item.qtyOnhand ?? 0) - Number(item.qtyReserved ?? 0) - Number(item.qtyFrozen ?? 0)
}

/**
 * 质量状态对应的 wd-tag 类型。
 *
 * 取值与 sql/deploy/04-dict.sql 的 wms_quality_status 字典 color_type 对齐：
 * QUALIFIED=success、QC_PENDING=warning、REJECTED=danger。
 * FROZEN 的字典色是 info，但 wd-tag 没有 info 这个 type，用 default 表达「中性/不可用」；
 * 字典里没有的值同样回落 default，不臆造颜色。
 */
const QUALITY_STATUS_TAG_TYPE: Record<string, TagType> = {
  QUALIFIED: 'success',
  QC_PENDING: 'warning',
  REJECTED: 'danger',
  FROZEN: 'default',
}

export function qualityStatusTagType(value?: string | null): TagType {
  return (value && QUALITY_STATUS_TAG_TYPE[value]) || 'default'
}
