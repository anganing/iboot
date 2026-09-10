<script lang="ts" setup>
import type { BpmApprovalDetail } from '@/api/bpm'
import { getApprovalDetail } from '@/api/bpm'

defineOptions({ name: 'ProcessDetail' })

definePage({
  style: {
    navigationBarTitleText: '流程详情',
  },
})

const detail = ref<BpmApprovalDetail>()
const loading = ref(true)
const query = ref({ activityId: '', processInstanceId: '', taskId: '' })
const statusTextMap: Record<number, string> = {
  '-2': '已跳过',
  0: '等待处理',
  1: '处理中',
  2: '已通过',
  3: '已拒绝',
  4: '已取消',
  5: '已退回',
  7: '处理中',
}

function getStatusText(status?: number) {
  return status === undefined ? '待处理' : statusTextMap[status] || '待处理'
}

function getNodeUserName(node: NonNullable<BpmApprovalDetail['activityNodes']>[number]) {
  return node.tasks?.[0]?.assigneeUser?.nickname || '待分配'
}

async function loadDetail() {
  if (!query.value.processInstanceId)
    return
  loading.value = true
  try {
    detail.value = await getApprovalDetail({
      processInstanceId: query.value.processInstanceId,
      taskId: query.value.taskId || undefined,
      activityId: query.value.activityId || undefined,
    })
  }
  finally {
    loading.value = false
  }
}

onLoad((options) => {
  query.value = {
    processInstanceId: String(options?.processInstanceId || ''),
    taskId: String(options?.taskId || ''),
    activityId: String(options?.activityId || ''),
  }
  loadDetail()
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] p-4">
    <view v-if="loading" class="pt-20 text-center text-sm text-gray-400">加载中...</view>
    <template v-else-if="detail?.processInstance">
      <view class="rounded-lg bg-white p-4 shadow-sm">
        <text class="block text-lg text-gray-900 font-semibold">{{ detail.processInstance.name }}</text>
        <view class="mt-3 flex items-center justify-between text-sm text-gray-500">
          <text>发起人：{{ detail.processInstance.startUser?.nickname || '-' }}</text>
          <text>{{ getStatusText(detail.status) }}</text>
        </view>
        <view v-if="detail.processInstance.summary?.length" class="mt-4 border-t border-[#edf0f4] pt-3">
          <view v-for="item in detail.processInstance.summary" :key="item.key" class="mb-2 flex text-sm">
            <text class="w-24 shrink-0 text-gray-400">{{ item.key }}</text>
            <text class="min-w-0 flex-1 break-all text-gray-700">{{ item.value }}</text>
          </view>
        </view>
      </view>

      <view class="mt-5">
        <text class="px-1 text-base text-gray-800 font-semibold">流程记录</text>
        <view class="mt-3 rounded-lg bg-white px-4 py-2 shadow-sm">
          <view v-for="(node, index) in detail.activityNodes" :key="node.id" class="relative flex py-3">
            <view class="timeline-line" :class="index === detail.activityNodes.length - 1 ? 'is-last' : ''" />
            <view class="timeline-dot" :class="node.status === 2 ? 'is-complete' : ''" />
            <view class="ml-4 min-w-0 flex-1">
              <view class="flex items-center justify-between">
                <text class="truncate text-sm text-gray-800 font-medium">{{ node.name }}</text>
                <text class="text-xs text-gray-400">{{ getStatusText(node.status) }}</text>
              </view>
              <text class="mt-1 block text-xs text-gray-500">{{ getNodeUserName(node) }}</text>
              <text v-if="node.tasks?.[0]?.reason" class="mt-1 block text-xs text-gray-400">{{ node.tasks[0].reason }}</text>
            </view>
          </view>
        </view>
      </view>
    </template>
    <view v-else class="pt-20 text-center text-sm text-gray-400">未找到流程详情</view>
  </view>
</template>

<style lang="scss" scoped>
.timeline-line {
  position: absolute;
  left: 7px;
  top: 24px;
  bottom: -12px;
  width: 1px;
  background: #dfe5ee;
}

.timeline-line.is-last {
  display: none;
}

.timeline-dot {
  position: relative;
  z-index: 1;
  height: 15px;
  width: 15px;
  flex: none;
  border: 3px solid #c9d2df;
  border-radius: 50%;
  background: #fff;
  box-sizing: border-box;
}

.timeline-dot.is-complete {
  border-color: var(--wot-color-theme, #1c64fd);
  background: var(--wot-color-theme, #1c64fd);
}
</style>
