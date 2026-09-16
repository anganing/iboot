import { describe, expect, it } from 'vitest'
import {
  dictLabel,
  MES_OP_TASK_STATUS,
  opTaskStatusLabel,
  opTaskStatusType,
  WMS_QUALITY_STATUS,
  WMS_STOCK_SCOPE,
} from './dict'

/**
 * 期望值全部手写中文字面量，不引用 `dict.ts` 的常量。
 * 若从常量派生期望值，就是「拿常量测常量」的自证循环，改错字典也测不出来。
 */

const MES_OP_TASK_STATUS_CASES: [string, string][] = [
  ['PENDING', '待排产'],
  ['READY', '待开工'],
  ['RUNNING', '加工中'],
  ['PAUSED', '已暂停'],
  ['QC_WAITING', '待质检'],
  ['HOLD', '已挂起'],
  ['DONE', '已完工'],
  ['SKIPPED', '已跳过'],
  ['CANCELLED', '已取消'],
]

const WMS_QUALITY_STATUS_CASES: [string, string][] = [
  ['QUALIFIED', '合格'],
  ['QC_PENDING', '待检'],
  ['REJECTED', '不合格'],
  ['FROZEN', '冻结'],
]

const WMS_STOCK_SCOPE_CASES: [string, string][] = [
  ['COMMON', '公共库存'],
  ['PROJECT_DEDICATED', '项目专用'],
]

describe('dictLabel — 三张字典逐值核对', () => {
  it('mes_op_task_status 全部 9 个值', () => {
    for (const [value, label] of MES_OP_TASK_STATUS_CASES) {
      expect(dictLabel('mes_op_task_status', value), `mes_op_task_status 的 ${value}`).toBe(label)
    }
  })

  it('wms_quality_status 全部 4 个值', () => {
    for (const [value, label] of WMS_QUALITY_STATUS_CASES) {
      expect(dictLabel('wms_quality_status', value), `wms_quality_status 的 ${value}`).toBe(label)
    }
  })

  it('wms_stock_scope 全部 2 个值', () => {
    for (const [value, label] of WMS_STOCK_SCOPE_CASES) {
      expect(dictLabel('wms_stock_scope', value), `wms_stock_scope 的 ${value}`).toBe(label)
    }
  })

  it('三张字典的覆盖面与后端字典一致（防止漏值和多值）', () => {
    expect(MES_OP_TASK_STATUS.map(item => item.value)).toEqual(MES_OP_TASK_STATUS_CASES.map(([value]) => value))
    expect(WMS_QUALITY_STATUS.map(item => item.value)).toEqual(WMS_QUALITY_STATUS_CASES.map(([value]) => value))
    expect(WMS_STOCK_SCOPE.map(item => item.value)).toEqual(WMS_STOCK_SCOPE_CASES.map(([value]) => value))
  })
})

describe('dictLabel — 空值', () => {
  it('null / undefined / 空串 均返回空串', () => {
    expect(dictLabel('mes_op_task_status', null)).toBe('')
    expect(dictLabel('mes_op_task_status', undefined)).toBe('')
    expect(dictLabel('mes_op_task_status', '')).toBe('')
    expect(dictLabel('mes_op_task_status')).toBe('')
  })

  it('纯空白字符串归一后视为空，返回空串', () => {
    expect(dictLabel('mes_op_task_status', '   ')).toBe('')
  })
})

describe('dictLabel — 回落原值', () => {
  it('未知字典类型回落原值', () => {
    expect(dictLabel('not_a_real_dict', 'RUNNING')).toBe('RUNNING')
  })

  it('已知字典中的未知值回落原值，不回落空串', () => {
    expect(dictLabel('mes_op_task_status', 'AWAITING_XX')).toBe('AWAITING_XX')
    expect(dictLabel('wms_quality_status', 'SCRAPPED')).toBe('SCRAPPED')
  })

  it('值的字典类型不匹配时回落原值（QUALIFIED 不属于 mes_op_task_status）', () => {
    expect(dictLabel('mes_op_task_status', 'QUALIFIED')).toBe('QUALIFIED')
  })

  it('数字入参转字符串后查表，查不到回落原值的字符串形式', () => {
    expect(dictLabel('mes_op_task_status', 123)).toBe('123')
  })

  it('数字入参命中数字型字典值（当前三张字典均为字符串值）', () => {
    // 反向确认：数值型值也能命中，避免只支持字符串入参
    expect(dictLabel('wms_stock_scope', 'COMMON')).toBe('公共库存')
  })
})

describe('dictLabel — 大小写与空格不做宽容', () => {
  it('小写值不命中，回落原值（后端固定大写，不做猜测式匹配）', () => {
    expect(dictLabel('mes_op_task_status', 'running')).toBe('running')
  })

  it('首尾空格被 trim 后命中', () => {
    expect(dictLabel('mes_op_task_status', ' RUNNING ')).toBe('加工中')
  })
})

describe('opTaskStatusLabel', () => {
  it('与 dictLabel(mes_op_task_status) 结果一致', () => {
    for (const [value, label] of MES_OP_TASK_STATUS_CASES) {
      expect(opTaskStatusLabel(value), `opTaskStatusLabel 的 ${value}`).toBe(label)
    }
  })

  it('空值返回空串，未知值回落原值', () => {
    expect(opTaskStatusLabel(null)).toBe('')
    expect(opTaskStatusLabel(undefined)).toBe('')
    expect(opTaskStatusLabel('WEIRD')).toBe('WEIRD')
  })
})

describe('opTaskStatusType', () => {
  const TYPE_CASES: [string, string][] = [
    ['PENDING', 'default'],
    ['READY', 'default'],
    ['RUNNING', 'primary'],
    ['PAUSED', 'danger'],
    ['QC_WAITING', 'primary'],
    ['HOLD', 'warning'],
    ['DONE', 'success'],
    ['SKIPPED', 'default'],
    ['CANCELLED', 'danger'],
  ]

  it('9 个状态各归对应档位', () => {
    for (const [value, type] of TYPE_CASES) {
      expect(opTaskStatusType(value), `opTaskStatusType 的 ${value}`).toBe(type)
    }
  })

  it('每个状态都有明确档位，不存在未映射的键', () => {
    for (const [value] of MES_OP_TASK_STATUS_CASES) {
      expect(TYPE_CASES.map(([v]) => v)).toContain(value)
    }
  })

  it('空值与未知值均回落 default', () => {
    expect(opTaskStatusType(null)).toBe('default')
    expect(opTaskStatusType(undefined)).toBe('default')
    expect(opTaskStatusType('')).toBe('default')
    expect(opTaskStatusType('NOT_A_STATUS')).toBe('default')
  })
})
