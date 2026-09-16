import type { TraceNode } from '@/api/mes/trace'
import { pickNumber, pickText, pickTime, toSnapshotEntries } from '@/api/mes/trace'

/**
 * 拍平后的一行。
 *
 * 树形渲染刻意不依赖递归组件：微信小程序端 `<template>` 递归组件写法受限，
 * 拍平成数组后由单层 `v-for` 渲染、用 `depth` 控制缩进，两端行为一致且最可靠。
 */
export interface TraceFlatNode {
  /** 行主键：父路径 + 同层序号，全树唯一（同一张工单可能从两个批次汇入，bizNo 不保证唯一） */
  key: string
  /** 原始节点 */
  node: TraceNode
  /** 层级深度，起点为 1 */
  depth: number
  /** 是否还有可见子节点 */
  hasChildren: boolean
  /** 当前是否展开（有自己的子节点才有效） */
  expanded: boolean
  /** 快照摘要，展示在卡片次级区 */
  entries: { label: string, value: string }[]
}

/** 各节点类型在快照里优先展示的时间字段 */
const TIME_KEYS: Record<string, string[]> = {
  BATCH: ['produceDate', 'txnTime'],
  OUT_ORDER: ['txnTime'],
  IN_ORDER: ['txnTime'],
  PROD_ORDER: ['actualStartTime', 'actualEndTime'],
  OP_TASK: ['actualStartTime', 'actualEndTime'],
  QC_TASK: ['inspectStartTime', 'inspectEndTime'],
  WIP_TXN: ['txnTime'],
  SUPPLIER: [],
  CUSTOMER: [],
}

/** 各节点类型在快照里优先展示的物料名 */
const MATERIAL_KEYS: Record<string, string[]> = {
  BATCH: ['matName'],
  PROD_ORDER: ['matName'],
  OP_TASK: ['matName'],
  QC_TASK: ['matName'],
  WIP_TXN: ['matName'],
}

/**
 * 按节点类型抽取要展示的摘要字段。
 *
 * 九类节点的快照键各不相同（后端各 resolver 自己塞的），这里只挑对现场判断有用的几个，
 * 全部走 pickText / pickNumber / pickTime 安全取值——键缺失是常态，不是异常。
 */
function buildEntries(node: TraceNode): { label: string, value: string }[] {
  const snapshot = node.snapshot
  const nodeType = node.nodeType
  const fields: [string, string][] = []

  // 第一行固定放物料：批次 / 工单 / 工序 / 质检 / 在制流水都带 matName
  const materialKeys = MATERIAL_KEYS[nodeType]
  if (materialKeys) {
    const matCode = pickText(snapshot, ['matCode'])
    const matName = pickText(snapshot, materialKeys)
    const material = [matCode, matName].filter(Boolean).join(' ')
    if (material)
      fields.push(['物料', material])
  }

  switch (nodeType) {
    case 'BATCH': {
      const supplierLotNo = pickText(snapshot, ['supplierLotNo'])
      if (supplierLotNo)
        fields.push(['供应商批次', supplierLotNo])
      const furnaceLotNo = pickText(snapshot, ['furnaceLotNo'])
      if (furnaceLotNo)
        fields.push(['炉批号', furnaceLotNo])
      const qty = pickNumber(snapshot, ['qty'])
      if (qty !== undefined)
        fields.push(['数量', String(qty)])
      break
    }
    case 'PROD_ORDER': {
      const qty = pickNumber(snapshot, ['qty'])
      const completedQty = pickNumber(snapshot, ['completedQty'])
      if (qty !== undefined)
        fields.push(['计划数量', String(qty)])
      if (completedQty !== undefined)
        fields.push(['完工数量', String(completedQty)])
      const scrapQty = pickNumber(snapshot, ['scrapQty'])
      if (scrapQty !== undefined)
        fields.push(['报废数量', String(scrapQty)])
      break
    }
    case 'OP_TASK': {
      const opName = pickText(snapshot, ['opName'])
      if (opName)
        fields.push(['工序', opName])
      const plannedQty = pickNumber(snapshot, ['plannedQty'])
      const completedQty = pickNumber(snapshot, ['completedQty'])
      if (plannedQty !== undefined)
        fields.push(['计划数量', String(plannedQty)])
      if (completedQty !== undefined)
        fields.push(['完成数量', String(completedQty)])
      break
    }
    case 'QC_TASK': {
      const qtyQualified = pickNumber(snapshot, ['qtyQualified'])
      const qtyRejected = pickNumber(snapshot, ['qtyRejected'])
      if (qtyQualified !== undefined)
        fields.push(['合格数', String(qtyQualified)])
      if (qtyRejected !== undefined)
        fields.push(['不合格数', String(qtyRejected)])
      break
    }
    case 'WIP_TXN': {
      const qty = pickNumber(snapshot, ['qty'])
      if (qty !== undefined)
        fields.push(['数量', String(qty)])
      break
    }
    case 'IN_ORDER': {
      const qty = pickNumber(snapshot, ['qty'])
      if (qty !== undefined)
        fields.push(['数量', String(qty)])
      const sourceBizNo = pickText(snapshot, ['sourceBizNo'])
      if (sourceBizNo)
        fields.push(['来源单据', sourceBizNo])
      break
    }
    case 'OUT_ORDER': {
      const qty = pickNumber(snapshot, ['qty'])
      if (qty !== undefined)
        fields.push(['数量', String(qty)])
      const materialReqNo = pickText(snapshot, ['materialReqNo'])
      if (materialReqNo)
        fields.push(['领料申请', materialReqNo])
      break
    }
    case 'SUPPLIER': {
      const lotNo = pickText(snapshot, ['lotNo'])
      if (lotNo)
        fields.push(['批次号', lotNo])
      const supplierLotNo = pickText(snapshot, ['supplierLotNo'])
      if (supplierLotNo)
        fields.push(['供应商批次', supplierLotNo])
      const furnaceLotNo = pickText(snapshot, ['furnaceLotNo'])
      if (furnaceLotNo)
        fields.push(['炉批号', furnaceLotNo])
      break
    }
    case 'CUSTOMER': {
      const customerCode = pickText(snapshot, ['customerCode'])
      if (customerCode)
        fields.push(['客户编码', customerCode])
      break
    }
  }

  fields.push(['时间', pickTime(snapshot, TIME_KEYS[nodeType] ?? [])])
  return toSnapshotEntries(fields)
}

/**
 * 把后端链路树拍平成一行行数组。
 *
 * 折叠时整枝跳过——被折叠子节点的子孙也不会进入结果集，展开即整体重现。
 *
 * @param root 后端返回的根节点，可能为空
 * @param expandedKeys 已展开节点的 key 集合
 */
export function flattenTraceTree(root: TraceNode | null | undefined, expandedKeys: Set<string>): TraceFlatNode[] {
  if (!root?.bizNo)
    return []
  const rows: TraceFlatNode[] = []

  const walk = (node: TraceNode, key: string, depth: number) => {
    const children = (node.children ?? []).filter(child => !!child?.bizNo)
    const hasChildren = children.length > 0
    const expanded = hasChildren && expandedKeys.has(key)
    rows.push({
      key,
      node,
      depth,
      hasChildren,
      expanded,
      entries: buildEntries(node),
    })
    if (expanded) {
      children.forEach((child, index) => walk(child, `${key}.${index}`, depth + 1))
    }
  }

  walk(root, '0', root.depth ?? 1)
  return rows
}

/** 收集全树节点 key 与节点总数，用于「默认全展开」与结果统计 */
export function collectTraceStats(root: TraceNode | null | undefined): { keys: string[], total: number } {
  if (!root?.bizNo)
    return { keys: [], total: 0 }
  const keys: string[] = []
  let total = 0

  const walk = (node: TraceNode, key: string) => {
    total += 1
    keys.push(key)
    const children = (node.children ?? []).filter(child => !!child?.bizNo)
    if (children.length > 0)
      children.forEach((child, index) => walk(child, `${key}.${index}`))
  }

  walk(root, '0')
  return { keys, total }
}
