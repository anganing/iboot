import { http } from '@/http/http'

/** 追溯查询起点类型，与后端 MesTraceStartTypeEnum 一致 */
export type TraceStartType = 'LOT' | 'PROD_ORDER' | 'SALES_OUT'

/** 追溯方向，与后端 MesTraceDirectionEnum 一致（正向到客户，反向到供应商） */
export type TraceDirection = 'BACKWARD' | 'FORWARD'

/**
 * 链路树节点类型，与后端 MesTraceNodeTypeEnum 一致（9 类）。
 *
 * 页面渲染时用 {@link TRACE_NODE_TYPE_LABEL} 映射中文，后端枚举的注释文案即此处取值，
 * 不要依赖后端透出的原始英文值直接展示。
 */
export type TraceNodeType
  = | 'BATCH'
    | 'CUSTOMER'
    | 'IN_ORDER'
    | 'OP_TASK'
    | 'OUT_ORDER'
    | 'PROD_ORDER'
    | 'QC_TASK'
    | 'SUPPLIER'
    | 'WIP_TXN'

/**
 * 节点关键字段快照：九类节点各有自己的字段，后端用 Java Map 原样透出，键随节点类型不同。
 *
 * **值是 Jackson 出口的原样形态**——`LocalDateTime` 序列化成毫秒时间戳数字、`LocalDate` 是
 * `'yyyy-MM-dd'` 字符串、其余是标量或数组。因此这里只能定成 `unknown`，
 * 取值一律走 {@link pickText} / {@link pickNumber} / {@link pickTime} 三个安全读取函数，
 * 禁止假定某个键一定存在。
 */
export type TraceSnapshot = Record<string, unknown>

/** 追溯链路树节点（后端 TraceNodeRespVO） */
export interface TraceNode {
  /** 节点类型 */
  nodeType: TraceNodeType | string
  /** 业务单号或批次号，节点自然键 */
  bizNo: string
  /** 节点名称（物料名 / 客户名 / 工序名等） */
  nodeName?: string
  /** 关键字段快照 */
  snapshot?: TraceSnapshot
  /** 层级深度，起点为 1 */
  depth?: number
  /** 是否因超过深度上限被截断 */
  truncated?: boolean
  /** 子节点 */
  children?: TraceNode[]
}

/** 追溯查询参数 */
export interface TraceQuery {
  /** 起点类型 */
  startType: TraceStartType
  /** 起点值：批次号 / 生产工单号 / 销售出库单号 */
  startValue: string
}

/** 追溯树查询参数：查询参数 + 方向，页面只调 {@link getTraceTree} 一个入口 */
export type TraceTreeQuery = TraceQuery & { direction: TraceDirection }

/** 节点类型中文标签，与本任务页面的渲染一致 */
export const TRACE_NODE_TYPE_LABEL: Record<string, string> = {
  BATCH: '批次',
  PROD_ORDER: '生产工单',
  OP_TASK: '工序任务',
  WIP_TXN: '在制流水',
  QC_TASK: '质检任务',
  IN_ORDER: '入库单',
  OUT_ORDER: '出库单',
  SUPPLIER: '供应商',
  CUSTOMER: '客户',
}

/** 起点类型选项，页面 segmented 直接用 */
export const TRACE_START_TYPE_OPTIONS: { label: string, value: TraceStartType, placeholder: string }[] = [
  { label: '批次号', value: 'LOT', placeholder: '请输入批次号，如 LOT20260801' },
  { label: '工单号', value: 'PROD_ORDER', placeholder: '请输入生产工单号' },
  { label: '出库单号', value: 'SALES_OUT', placeholder: '请输入销售出库单号' },
]

/** 节点类型中文标签，未知类型回退原始值 */
export function getNodeTypeLabel(nodeType?: string): string {
  if (!nodeType)
    return '未知节点'
  return TRACE_NODE_TYPE_LABEL[nodeType] ?? nodeType
}

/** 从快照里按候选键取第一个非空标量并转成字符串，缺失返回空串 */
export function pickText(snapshot: TraceSnapshot | undefined, keys: string[]): string {
  if (!snapshot)
    return ''
  for (const key of keys) {
    const value = snapshot[key]
    if (value === null || value === undefined || value === '')
      continue
    if (typeof value === 'string' || typeof value === 'number' || typeof value === 'boolean')
      return String(value)
  }
  return ''
}

/** 从快照里按候选键取第一个可解析为数字的值，缺失或不是数字返回 undefined */
export function pickNumber(snapshot: TraceSnapshot | undefined, keys: string[]): number | undefined {
  if (!snapshot)
    return undefined
  for (const key of keys) {
    const value = snapshot[key]
    if (value === null || value === undefined || value === '')
      continue
    const num = typeof value === 'number' ? value : Number(value)
    if (!Number.isNaN(num))
      return num
  }
  return undefined
}

/**
 * 从快照里取时间并格式化成 `MM-DD HH:mm`。
 *
 * 后端 `LocalDateTime` 是毫秒时间戳数字、`LocalDate` 是 `'yyyy-MM-dd'` 字符串；
 * 纯日期没有时分，按 `MM-DD` 返回，缺失返回空串。
 */
export function pickTime(snapshot: TraceSnapshot | undefined, keys: string[]): string {
  if (!snapshot)
    return ''
  for (const key of keys) {
    const value = snapshot[key]
    if (value === null || value === undefined || value === '')
      continue
    // 纯日期字符串：没有时分，只回月日
    if (typeof value === 'string' && /^\d{4}-\d{2}-\d{2}$/.test(value))
      return value.slice(5)
    // 后端可能把时间戳序列化成数字或纯数字字符串，两种都当时间戳处理
    const timestamp = typeof value === 'number'
      ? value
      : (typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : Number.NaN)
    if (Number.isNaN(timestamp))
      continue
    const date = new Date(timestamp)
    if (Number.isNaN(date.getTime()))
      continue
    const pad = (num: number) => String(num).padStart(2, '0')
    return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
  }
  return ''
}

/** 把节点快照转成可读的 `标签：值` 键值对列表，键顺序即定义顺序 */
export function toSnapshotEntries(entries: [string, string][]): { label: string, value: string }[] {
  return entries
    .filter(([, value]) => value !== '')
    .map(([label, value]) => ({ label, value }))
}

/** 批次正向追溯：从原料批次 / 生产工单 / 销售出库单出发，一次返回完整正向链路树 */
export function getTraceForward(params: TraceQuery) {
  return http.get<TraceNode>('/mes/trace/forward', params)
}

/** 批次反向追溯：一次反查到原料批次与供应商的完整链路树 */
export function getTraceBackward(params: TraceQuery) {
  return http.get<TraceNode>('/mes/trace/backward', params)
}

/** 按方向取对应端点，页面只用一个入口调它 */
export function getTraceTree(params: TraceTreeQuery) {
  return params.direction === 'BACKWARD'
    ? getTraceBackward(params)
    : getTraceForward(params)
}
