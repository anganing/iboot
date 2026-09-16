<script lang="ts" setup>
import type { TraceFlatNode } from './trace-flat'
import { getNodeTypeLabel } from '@/api/mes/trace'

defineOptions({ name: 'TraceNodeCard' })

const props = defineProps<{
  /** 拍平后的行数据 */
  row: TraceFlatNode
}>()

const emit = defineEmits<{
  /** 切换该节点的展开状态 */
  (e: 'toggle', key: string): void
}>()

/**
 * 节点配色：供应商用灰、客户用绿、质检用橙，其余用主题蓝。
 *
 * 走 wd-tag 的 color / bg-color 两个属性，不往 type 里塞色值——type 只吃固定枚举。
 */
const NODE_TONE: Record<string, { color: string, bgColor: string }> = {
  SUPPLIER: { color: '#6b7280', bgColor: '#f1f3f5' },
  CUSTOMER: { color: '#16a36a', bgColor: '#eaf8f1' },
  QC_TASK: { color: '#e68228', bgColor: '#fff3e8' },
}

const DEFAULT_TONE = { color: 'var(--wot-color-theme, #1c64fd)', bgColor: '#e8f0ff' }

const tone = computed(() => NODE_TONE[props.row.node.nodeType] ?? DEFAULT_TONE)

const typeLabel = computed(() => getNodeTypeLabel(props.row.node.nodeType))

/** 缩进按层级递进，每层 24rpx */
const indentStyle = computed(() => {
  const depth = Math.max(props.row.depth, 1) - 1
  return { marginLeft: `${depth * 24}rpx` }
})

function onToggle() {
  if (props.row.hasChildren)
    emit('toggle', props.row.key)
}
</script>

<template>
  <view class="trace-node" :style="indentStyle">
    <view class="trace-node__card" :class="{ 'trace-node__card--nested': row.depth > 1 }">
      <view class="flex items-center" @click="onToggle">
        <wd-icon
          v-if="row.hasChildren"
          :name="row.expanded ? 'arrow-down' : 'arrow-right'"
          size="14px"
          class="shrink-0 text-gray-400"
        />
        <view v-else class="shrink-0" style="width: 14px" />

        <wd-tag
          :color="tone.color"
          :bg-color="tone.bgColor"
          size="small"
          class="ml-2 shrink-0"
        >
          {{ typeLabel }}
        </wd-tag>

        <text class="ml-2 min-w-0 flex-1 truncate text-sm text-gray-800 font-semibold">{{ row.node.bizNo }}</text>

        <text v-if="row.hasChildren" class="ml-1 shrink-0 text-2xs text-gray-400">
          {{ row.node.children?.length }}
        </text>
      </view>

      <text v-if="row.node.nodeName" class="mt-1 block pl-6 text-xs text-gray-500">{{ row.node.nodeName }}</text>

      <view v-if="row.entries.length" class="mt-2 pl-6">
        <view v-for="entry in row.entries" :key="entry.label" class="flex items-baseline">
          <text class="w-20 shrink-0 text-2xs text-gray-400">{{ entry.label }}</text>
          <text class="min-w-0 flex-1 break-all text-xs text-gray-600">{{ entry.value }}</text>
        </view>
      </view>

      <view v-if="row.node.truncated" class="ml-6 mt-2 flex items-center rounded bg-[#fff7e6] px-2 py-1">
        <wd-icon name="warning" size="14px" class="shrink-0 text-[#e68228]" />
        <text class="ml-1 text-2xs text-[#b45309]">已截断，链路过深</text>
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.trace-node {
  position: relative;
  margin-bottom: 12rpx;
}

.trace-node__card {
  padding: 20rpx 24rpx;
  border-radius: 12rpx;
  background-color: #fff;
  box-shadow: 0 1px 4rpx rgba(15, 23, 42, 0.06);
  box-sizing: border-box;
}

// 非根节点用左竖线体现层级归属
.trace-node__card--nested {
  border-left: 4rpx solid #dfe5ee;
}
</style>
