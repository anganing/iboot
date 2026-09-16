import type { TagType } from '@wot-ui/ui/components/wd-tag/types'

/**
 * 状态字典映射工具
 *
 * 后端返回的是枚举原始值（如 `RUNNING`、`QUALIFIED`），直接展示给车间/库房用户
 * 是没有意义的。本文件把原始值映射成中文。
 *
 * 字典值取自后端 `system_dict_data` 实测数据，**直接写死在常量里**——移动端不引
 * 字典接口，省一次请求。后端新增字典值时需要同步维护本文件。
 *
 * 本文件保持零依赖（只 import 类型），不得 import `src/utils/index.ts`——后者顶层
 * 引入 `@/pages.json` 并调用 `uni.getAccountInfoSync()` 等平台 API，纯单测下加载即挂。
 */

/** 一个字典项：原始值 ↔ 中文标签 */
interface DictItem {
  /** 后端返回的原始值 */
  value: string
  /** 展示用中文 */
  label: string
}

/** 字典类型 → 字典项列表 */
type DictMap = Record<string, readonly DictItem[]>

/**
 * 工序任务状态
 * 对应后端字典类型 `mes_op_task_status`
 */
export const MES_OP_TASK_STATUS: readonly DictItem[] = [
  { value: 'PENDING', label: '待排产' },
  { value: 'READY', label: '待开工' },
  { value: 'RUNNING', label: '加工中' },
  { value: 'PAUSED', label: '已暂停' },
  { value: 'QC_WAITING', label: '待质检' },
  { value: 'HOLD', label: '已挂起' },
  { value: 'DONE', label: '已完工' },
  { value: 'SKIPPED', label: '已跳过' },
  { value: 'CANCELLED', label: '已取消' },
]

/**
 * 库存质量状态
 * 对应后端字典类型 `wms_quality_status`
 */
export const WMS_QUALITY_STATUS: readonly DictItem[] = [
  { value: 'QUALIFIED', label: '合格' },
  { value: 'QC_PENDING', label: '待检' },
  { value: 'REJECTED', label: '不合格' },
  { value: 'FROZEN', label: '冻结' },
]

/**
 * 库存归属范围
 * 对应后端字典类型 `wms_stock_scope`
 */
export const WMS_STOCK_SCOPE: readonly DictItem[] = [
  { value: 'COMMON', label: '公共库存' },
  { value: 'PROJECT_DEDICATED', label: '项目专用' },
]

/**
 * 派工任务状态
 * 对应后端字典类型 `mes_dispatch_task_status`
 *
 * ⚠️ **与 `mes_op_task_status` 不是同一套字典，不能互相替代。**
 * 两者都有 `PENDING` / `RUNNING` / `DONE` / `CANCELLED` 这几个值，但语义完全不同：
 * 派工状态描述的是「工人有没有接单」（待接单 / 已接单），
 * 工序任务状态描述的是「这道工序在生产上到哪一步了」（待排产 / 待开工 / 加工中）。
 * 拿工序字典渲染派工状态，`PENDING` 会显示成「待排产」而正确答案是「待接单」——
 * 不报错、不崩溃，就是静默显示错的业务含义。
 *
 * 后端侧同样区分：`DispatchTaskRespVO.status` 标注的是
 * `@DictFormat(MesDictTypeConstants.DISPATCH_TASK_STATUS)`。
 */
export const MES_DISPATCH_TASK_STATUS: readonly DictItem[] = [
  { value: 'PENDING', label: '待接单' },
  { value: 'ACCEPTED', label: '已接单' },
  { value: 'RUNNING', label: '作业中' },
  { value: 'DONE', label: '已完成' },
  { value: 'CANCELLED', label: '已取消' },
]

/** 字典类型 → 字典项，供 `dictLabel` 做通用查表 */
const DICT_MAP: DictMap = {
  mes_op_task_status: MES_OP_TASK_STATUS,
  mes_dispatch_task_status: MES_DISPATCH_TASK_STATUS,
  wms_quality_status: WMS_QUALITY_STATUS,
  wms_stock_scope: WMS_STOCK_SCOPE,
}

/**
 * 工序任务状态 → 展示色
 *
 * 返回值是 wot-ui v2 `wd-tag` 的 `type` 枚举名，调用方直接 `:type="opTaskStatusType(s)"`
 * 即可，组件自带配套的浅色底与文字色，不需要我们再维护一套色板。
 *
 * 档位划分：
 * - 进行中（`RUNNING` / `QC_WAITING`）→ `primary`
 * - 已完成（`DONE`）→ `success`
 * - 异常（`PAUSED` / `CANCELLED`）→ `danger`
 * - 挂起（`HOLD`）→ `warning`
 * - 待办/未进入在制（`PENDING` / `READY` / `SKIPPED`）→ `default`
 *
 * `SKIPPED` 归中性档：工序一旦跳过就不再是在制品，给 `success` 会误示为已完成，
 * 给 `danger` 又过重，靠标签文案「已跳过」区分即可。
 */
const OP_TASK_STATUS_TYPE: Record<string, TagType> = {
  PENDING: 'default',
  READY: 'default',
  RUNNING: 'primary',
  PAUSED: 'danger',
  QC_WAITING: 'primary',
  HOLD: 'warning',
  DONE: 'success',
  SKIPPED: 'default',
  CANCELLED: 'danger',
}

/** 把入参归一成可查表的字符串；空值返回空串 */
function normalize(value?: string | number | null): string {
  if (value === null || value === undefined) {
    return ''
  }
  return String(value).trim()
}

/**
 * 按字典类型取标签
 *
 * 查不到时**回落原值**（不是空串）——页面上出现 `AWAITING_XX` 时能直接看出是哪个值
 * 没被映射到，回落空串则连排查线索都没有。
 *
 * @param dictType 字典类型，如 `mes_op_task_status`
 * @param value 后端返回的原始值
 * @returns 中文标签；入参为空返回空串；字典类型或值未知时返回原值
 */
export function dictLabel(dictType: string, value?: string | number | null): string {
  const raw = normalize(value)
  if (!raw) {
    return ''
  }

  const hit = DICT_MAP[dictType]?.find(item => item.value === raw)
  return hit ? hit.label : raw
}

/**
 * 工序任务状态标签（快捷方式，列表高频用）
 *
 * @param value 后端返回的原始状态值
 * @returns 中文标签；入参为空返回空串；未知值返回原值
 */
export function opTaskStatusLabel(value?: string | null): string {
  return dictLabel('mes_op_task_status', value)
}

/**
 * 工序任务状态对应的展示色，供 `wd-tag` 的 `type` 使用
 *
 * @param value 后端返回的原始状态值
 * @returns wot-ui `wd-tag` 的 type 枚举名；未知值回落 `'default'`
 */
export function opTaskStatusType(value?: string | null): TagType {
  const raw = normalize(value)
  return OP_TASK_STATUS_TYPE[raw] ?? 'default'
}

/**
 * 派工任务状态 → 展示色
 *
 * 与 {@link opTaskStatusType} 分开维护：两套字典的值域不同（派工没有 `READY` / `QC_WAITING`
 * 等状态，多了 `ACCEPTED`），共用一张表会让某一侧出现永远命中不了的死条目。
 *
 * 后端字典 `color_type` 为：`PENDING=info`、`ACCEPTED=primary`、`RUNNING=warning`、
 * `DONE=success`、`CANCELLED=info`。其中 `info` 在 `wd-tag` 里没有对应枚举，
 * 用 `default` 表达中性（与 `wms_quality_status` 的 `FROZEN` 同样处理）。
 */
const DISPATCH_TASK_STATUS_TYPE: Record<string, TagType> = {
  PENDING: 'default',
  ACCEPTED: 'primary',
  RUNNING: 'warning',
  DONE: 'success',
  CANCELLED: 'default',
}

/**
 * 派工任务状态标签（快捷方式，列表高频用）
 *
 * ⚠️ 不要拿 {@link opTaskStatusLabel} 代替：两者值域重叠但语义不同，
 * `PENDING` 在派工里是「待接单」、在工序任务里是「待排产」。
 *
 * @param value 后端返回的原始状态值
 * @returns 中文标签；入参为空返回空串；未知值返回原值
 */
export function dispatchTaskStatusLabel(value?: string | null): string {
  return dictLabel('mes_dispatch_task_status', value)
}

/**
 * 派工任务状态对应的展示色，供 `wd-tag` 的 `type` 使用
 *
 * @param value 后端返回的原始状态值
 * @returns wot-ui `wd-tag` 的 type 枚举名；未知值回落 `'default'`
 */
export function dispatchTaskStatusType(value?: string | null): TagType {
  const raw = normalize(value)
  return DISPATCH_TASK_STATUS_TYPE[raw] ?? 'default'
}
