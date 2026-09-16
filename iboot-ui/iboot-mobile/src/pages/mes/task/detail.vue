<script lang="ts" setup>
import type { OpTask } from '@/api/mes/optask'
import { getOpTask } from '@/api/mes/optask'
import { opTaskStatusLabel, opTaskStatusType } from '@/utils/dict'

defineOptions({ name: 'MesTaskDetail' })

definePage({
  style: {
    navigationBarTitleText: '工序任务详情',
  },
})

const opTaskId = ref(0)
const detail = ref<OpTask>()
const loading = ref(true)

/** 数量归一，防 BigDecimal 序列化出的 100.0000 长尾 */
function formatQty(value?: number | null): string {
  const num = Number(value ?? 0)
  if (Number.isNaN(num))
    return '0'
  return String(Number(num.toFixed(3)))
}

function formatTime(value?: string) {
  if (!value)
    return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime()))
    return value
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

async function loadDetail() {
  if (!opTaskId.value)
    return
  loading.value = true
  try {
    detail.value = await getOpTask(opTaskId.value)
  }
  catch (error) {
    console.error('获取工序任务详情失败', error)
  }
  finally {
    loading.value = false
  }
}

/** 作业指导书入口：ESOP 按路线工序编号查询，没挂工序则没有指导书可看 */
function onOpenEsop() {
  const routeOpId = detail.value?.routeOpId
  if (!routeOpId) {
    uni.showToast({ icon: 'none', title: '该工序未配置作业指导书' })
    return
  }
  uni.navigateTo({ url: `/pages/mes/task/esop?routeOpId=${routeOpId}` })
}

/** 报工入口：报工页由 M4 提供，未落盘时给友好提示而不是静默失败 */
function onReport() {
  const task = detail.value
  if (!task?.id) {
    return
  }
  uni.navigateTo({
    url: `/pages/mes/task/report?opTaskId=${task.id}&taskNo=${encodeURIComponent(task.taskNo || '')}`,
    fail: () => uni.showToast({ icon: 'none', title: '报工页暂不可用' }),
  })
}

onLoad((options) => {
  opTaskId.value = Number(options?.opTaskId || 0)
  loadDetail()
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] p-4 pb-safe">
    <view v-if="loading" class="pt-20 text-center text-sm text-gray-400">
      加载中...
    </view>

    <template v-else-if="detail">
      <view class="rounded-lg bg-white p-4 shadow-sm">
        <view class="flex items-start">
          <view class="min-w-0 flex-1">
            <text class="block text-base text-gray-900 font-semibold">{{ detail.opName || '未命名工序' }}</text>
            <text class="mt-1 block text-xs text-gray-400">{{ detail.taskNo }}</text>
          </view>
          <wd-tag :type="opTaskStatusType(detail.status)" size="small" variant="light" class="shrink-0">
            {{ opTaskStatusLabel(detail.status) }}
          </wd-tag>
        </view>
        <view v-if="detail.keyFlag" class="mt-2">
          <wd-tag type="danger" size="small" variant="plain">
            关键工序
          </wd-tag>
        </view>
      </view>

      <!-- 数量进度是工人最关心的信息，放在最显眼位置 -->
      <view class="mt-3 rounded-lg bg-white p-4 shadow-sm">
        <text class="block text-2xs text-gray-400">完成进度</text>
        <view class="mt-2 flex items-baseline">
          <text class="text-3xl text-primary font-bold">{{ formatQty(detail.completedQty) }}</text>
          <text class="ml-1 text-sm text-gray-400">/ {{ formatQty(detail.plannedQty) }}</text>
        </view>
        <view class="mt-3 flex items-center border-t border-[#edf0f4] pt-3">
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">计划</text>
            <text class="mt-1 block text-sm text-gray-700">{{ formatQty(detail.plannedQty) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">合格</text>
            <text class="mt-1 block text-sm text-[#16a36a] font-semibold">{{ formatQty(detail.completedQty) }}</text>
          </view>
          <view class="flex-1 text-center">
            <text class="block text-2xs text-gray-400">报废</text>
            <text class="mt-1 block text-sm text-[#e68228] font-semibold">{{ formatQty(detail.scrapQty) }}</text>
          </view>
        </view>
      </view>

      <wd-cell-group custom-class="mt-3 rounded-2xl overflow-hidden">
        <wd-cell title="工序任务号" :value="detail.taskNo || '-'" />
        <wd-cell title="工序编码" :value="detail.opCode || '-'" />
        <wd-cell title="工序顺序" :value="detail.seq != null ? String(detail.seq) : '-'" />
        <wd-cell title="作业工单号" :value="detail.workOrderNo || '-'" />
        <wd-cell title="生产工单号" :value="detail.prodOrderNo || '-'" />
        <wd-cell title="生产资源" :value="detail.resName || '-'" />
        <wd-cell title="计划开始" :value="formatTime(detail.plannedStartTime)" />
        <wd-cell title="计划结束" :value="formatTime(detail.plannedEndTime)" />
        <wd-cell title="实际开始" :value="formatTime(detail.actualStartTime)" />
        <wd-cell title="实际结束" :value="formatTime(detail.actualEndTime)" />
        <wd-cell v-if="detail.remark" title="备注" :value="detail.remark" />
      </wd-cell-group>

      <!-- 底部固定双按钮：看指导书（只读）与报工（写入），是现场两个主要动作 -->
      <view class="mt-4 flex items-center">
        <wd-button
          plain
          custom-class="mr-2 flex-1"
          :disabled="!detail.routeOpId"
          @click="onOpenEsop"
        >
          查看作业指导书
        </wd-button>
        <wd-button type="primary" custom-class="flex-1" @click="onReport">
          报工
        </wd-button>
      </view>
    </template>

    <view v-else class="pt-20 text-center text-sm text-gray-400">
      未找到工序任务
    </view>
  </view>
</template>
