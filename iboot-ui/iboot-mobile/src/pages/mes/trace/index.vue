<script lang="ts" setup>
import type { TraceDirection, TraceNode, TraceStartType } from '@/api/mes/trace'
import type { TraceFlatNode } from './components/trace-flat'
import { getTraceTree, TRACE_START_TYPE_OPTIONS } from '@/api/mes/trace'
import { collectTraceStats, flattenTraceTree } from './components/trace-flat'
import TraceNodeCard from './components/trace-node-card.vue'

defineOptions({ name: 'MesTrace' })

definePage({
  style: {
    navigationBarTitleText: '批次追溯',
  },
})

const startTypeOptions = TRACE_START_TYPE_OPTIONS

/** 起点类型：segmented 绑的是中文 label，映射回后端枚举值 */
const startTypeLabel = ref(startTypeOptions[0].label)
const startType = computed<TraceStartType>(() => {
  const matched = startTypeOptions.find(item => item.label === startTypeLabel.value)
  return matched?.value ?? 'LOT'
})
const startValuePlaceholder = computed(() => {
  return startTypeOptions.find(item => item.label === startTypeLabel.value)?.placeholder ?? '请输入'
})

const startValue = ref('')

/** 方向：正向查客户，反向查供应商 */
const directionLabel = ref('正向（查客户）')
const direction = computed<TraceDirection>(() => (directionLabel.value === '反向（查供应商）' ? 'BACKWARD' : 'FORWARD'))
const directionOptions = ['正向（查客户）', '反向（查供应商）']

const loading = ref(false)
/** 是否已经查询过（区分「未查询」与「查不到」两种空态） */
const queried = ref(false)
const tree = ref<TraceNode | null>(null)
/** 展开节点的 key 集合，折叠时整枝收起 */
const expandedKeys = ref<Set<string>>(new Set())
/** 结果统计，展示在结果头部 */
const stats = ref({ nodeTotal: 0, depth: 0 })

/** 拍平后的可见行：单层 v-for 渲染，不依赖递归组件，小程序端最可靠 */
const rows = computed<TraceFlatNode[]>(() => flattenTraceTree(tree.value, expandedKeys.value))

const hasResult = computed(() => !!tree.value?.bizNo)

const directionSummary = computed(() => {
  return direction.value === 'BACKWARD' ? '反向：成品 → 原料供应商' : '正向：原料 → 出货客户'
})

/** 查询：一次拿回整棵树，不需要逐层调接口 */
async function onQuery() {
  const value = startValue.value.trim()
  if (!value) {
    uni.showToast({ title: '请输入起点值', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const result = await getTraceTree({
      startType: startType.value,
      startValue: value,
      direction: direction.value,
    })
    tree.value = result ?? null
    queried.value = true
    // 默认全展开：追溯的诉求是「一眼看完链路」，先给全貌再让用户收
    const { keys, total } = collectTraceStats(result)
    expandedKeys.value = new Set(keys)
    stats.value = { nodeTotal: total, depth: maxDepth(result) }
  }
  catch (error) {
    // http 封装已 toast，业务只需清空结果（hermes/api.md 第 3 节）
    console.error('批次追溯查询失败', error)
    tree.value = null
    queried.value = true
    expandedKeys.value = new Set()
    stats.value = { nodeTotal: 0, depth: 0 }
  }
  finally {
    loading.value = false
  }
}

/** 递归求树最大层级，仅用于结果头部展示 */
function maxDepth(node: TraceNode | null | undefined): number {
  if (!node?.bizNo)
    return 0
  const children = (node.children ?? []).filter(child => !!child?.bizNo)
  if (children.length === 0)
    return node.depth ?? 1
  return Math.max(...children.map(child => maxDepth(child)))
}

function onToggleNode(key: string) {
  const next = new Set(expandedKeys.value)
  if (next.has(key))
    next.delete(key)
  else
    next.add(key)
  expandedKeys.value = next
}

function onToggleAll() {
  if (expandedKeys.value.size > 0) {
    expandedKeys.value = new Set()
    return
  }
  expandedKeys.value = new Set(collectTraceStats(tree.value).keys)
}

const allExpanded = computed(() => expandedKeys.value.size > 0 && rows.value.length === stats.value.nodeTotal)

function onStartTypeChange() {
  // 换了起点类型，旧结果已经不对应，清掉避免误读
  onReset()
}

function onReset() {
  tree.value = null
  queried.value = false
  expandedKeys.value = new Set()
  stats.value = { nodeTotal: 0, depth: 0 }
}
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] pb-safe">
    <view class="trace-header px-4 pb-5 pt-4">
      <view class="rounded-xl bg-white p-4 shadow-sm">
        <text class="mb-2 block text-xs text-gray-400">起点类型</text>
        <wd-segmented
          v-model:value="startTypeLabel"
          :options="startTypeOptions.map(item => item.label)"
          theme="outline"
          custom-class="trace-segmented"
          @change="onStartTypeChange"
        />

        <text class="mb-2 mt-4 block text-xs text-gray-400">追溯方向</text>
        <wd-segmented
          v-model:value="directionLabel"
          :options="directionOptions"
          theme="outline"
          custom-class="trace-segmented"
        />

        <view class="mt-4">
          <wd-input
            v-model="startValue"
            :placeholder="startValuePlaceholder"
            clearable
            confirm-type="search"
            @confirm="onQuery"
          />
        </view>

        <view class="mt-4 flex items-center">
          <wd-button type="primary" block :loading="loading" @click="onQuery">
            开始追溯
          </wd-button>
        </view>

        <text class="mt-3 block text-2xs text-gray-400">{{ directionSummary }}</text>
      </view>
    </view>

    <view v-if="hasResult" class="px-4 pb-6">
      <view class="mb-3 flex items-center justify-between">
        <text class="text-xs text-gray-500">
          共 {{ stats.nodeTotal }} 个节点 · {{ stats.depth }} 层
        </text>
        <text class="text-xs text-primary" @click="onToggleAll">{{ allExpanded ? '全部收起' : '全部展开' }}</text>
      </view>

      <TraceNodeCard
        v-for="row in rows"
        :key="row.key"
        :row="row"
        @toggle="onToggleNode"
      />
    </view>

    <view v-else-if="queried" class="flex flex-col items-center pt-20">
      <wd-icon name="no-content" size="64px" class="text-gray-300" />
      <text class="mt-4 px-10 text-center text-sm text-gray-400">未找到追溯链路</text>
      <text class="mt-1 px-10 text-center text-2xs text-gray-300">请确认单号或批次号是否正确，或换个追溯方向试试</text>
    </view>

    <view v-else class="flex flex-col items-center pt-20">
      <wd-icon name="search" size="64px" class="text-gray-300" />
      <text class="mt-4 px-10 text-center text-sm text-gray-400">输入批次号或单号开始追溯</text>
      <text class="mt-1 px-10 text-center text-2xs text-gray-300">正向查到出货客户，反向查到原料供应商</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.trace-header {
  background: linear-gradient(180deg, #eef3ff 0%, #f5f7fb 100%);
}

:deep(.trace-segmented) {
  width: 100%;
}
</style>
