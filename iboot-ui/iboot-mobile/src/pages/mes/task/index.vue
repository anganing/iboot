<script lang="ts" setup>
import type { DispatchTask } from '@/api/mes/optask'
import { getDispatchTaskPage } from '@/api/mes/optask'
import { useUserStore } from '@/store/user'
import { dispatchTaskStatusLabel, dispatchTaskStatusType } from '@/utils/dict'

defineOptions({ name: 'MesTaskList' })

definePage({
  style: {
    navigationBarTitleText: '我的任务',
  },
})

type StatusFilter = 'ALL' | 'PENDING' | 'ACCEPTED' | 'RUNNING' | 'DONE'

/**
 * 筛选值必须取 `mes_dispatch_task_status` 的值域（PENDING / ACCEPTED / RUNNING / DONE / CANCELLED），
 * **不要用工序任务字典的 READY / QC_WAITING 等**。
 *
 * 后端 `DispatchTaskPageReqVO.status` 是裸 String、没有 `@InEnum` 校验，
 * 传错值不会报错，只会静默查不出数据——这种错误最难发现。
 */
const STATUS_FILTER_OPTIONS = ['全部', '待接单', '已接单', '作业中', '已完成']
const STATUS_FILTER_MAP: Record<string, StatusFilter> = {
  全部: 'ALL',
  待接单: 'PENDING',
  已接单: 'ACCEPTED',
  作业中: 'RUNNING',
  已完成: 'DONE',
}

const userStore = useUserStore()
const pagingRef = ref<ZPagingRef>()
const dataList = ref<DispatchTask[]>([])
const statusFilter = ref<StatusFilter>('ALL')
/**
 * 默认只看自己派到的活——工人打开「我的任务」想看的必然是自己的单。
 * 关掉后不传 operatorId，看全车间，供班组长排产/盯进度用。
 */
const onlyMine = ref(true)

const segmentedValue = computed({
  get: () => ({
    ALL: '全部',
    PENDING: '待接单',
    ACCEPTED: '已接单',
    RUNNING: '作业中',
    DONE: '已完成',
  })[statusFilter.value],
  set: (value: string) => {
    statusFilter.value = STATUS_FILTER_MAP[value] ?? 'ALL'
  },
})

/** 当前登录用户编号；未拉到用户信息时 store 里是哨兵值 -1 */
const currentUserId = computed(() => userStore.userInfo.userId)

async function queryList(pageNo: number, pageSize: number) {
  // userId 为 -1 说明登录态里还没有真实用户，拿它去筛只会查出空列表，
  // 反而让人误以为「我没任务」。这里直接不发请求并给出重新登录的提示。
  if (onlyMine.value && currentUserId.value <= 0) {
    uni.showToast({ icon: 'none', title: '未获取到登录信息，请重新登录' })
    pagingRef.value?.complete(false)
    return
  }

  try {
    const result = await getDispatchTaskPage({
      pageNo,
      pageSize,
      operatorId: onlyMine.value ? currentUserId.value : undefined,
      status: statusFilter.value === 'ALL' ? undefined : statusFilter.value,
    })
    pagingRef.value?.completeByTotal(result.list, result.total)
  }
  catch (error) {
    console.error('获取派工任务失败', error)
    pagingRef.value?.complete(false)
  }
}

/** 派工任务自己的状态字典（mes_dispatch_task_status），与工序任务状态不是同一套 */
function dispatchStatusLabel(value?: string) {
  return dispatchTaskStatusLabel(value)
}

function dispatchStatusType(value?: string) {
  return dispatchTaskStatusType(value)
}

function formatTime(value?: string) {
  if (!value)
    return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime()))
    return value
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function onFilterChange() {
  pagingRef.value?.reload()
}

function onItemClick(item: DispatchTask) {
  if (!item.opTaskId) {
    uni.showToast({ icon: 'none', title: '该派工单未关联工序任务' })
    return
  }
  uni.navigateTo({ url: `/pages/mes/task/detail?opTaskId=${item.opTaskId}` })
}
</script>

<template>
  <view class="h-full bg-[#f5f7fb]">
    <z-paging
      ref="pagingRef"
      v-model="dataList"
      :default-page-size="10"
      bg-color="#f5f7fb"
      empty-view-text="暂无派工任务"
      safe-area-inset-bottom
      @query="queryList"
    >
      <template #top>
        <view class="bg-white px-4 pb-3 pt-3">
          <view class="flex items-center justify-between">
            <text class="text-sm text-gray-700">只看我的任务</text>
            <view class="flex items-center">
              <text class="mr-2 text-xs text-gray-400">{{ userStore.userInfo.nickname || userStore.userInfo.username || '' }}</text>
              <wd-switch v-model="onlyMine" size="20px" @change="onFilterChange" />
            </view>
          </view>
          <view class="mt-3">
            <wd-segmented
              v-model:value="segmentedValue"
              :options="STATUS_FILTER_OPTIONS"
              theme="outline"
              custom-class="task-status-tabs"
              @change="onFilterChange"
            />
          </view>
        </view>
      </template>

      <view
        v-for="item in dataList"
        :key="item.id"
        class="mx-4 mt-3 rounded-lg bg-white p-4 shadow-sm"
        @click="onItemClick(item)"
      >
        <view class="flex items-start">
          <view class="min-w-0 flex-1">
            <view class="flex items-center">
              <text class="truncate text-sm text-gray-800 font-semibold">{{ item.opName || '未命名工序' }}</text>
              <text class="ml-2 shrink-0 text-xs text-gray-400">{{ item.opTaskNo || '' }}</text>
            </view>
            <view class="mt-1 flex items-center text-xs text-gray-400">
              <text class="truncate">派工单：{{ item.dispatchNo || '-' }}</text>
            </view>
            <view class="mt-1 flex items-center text-xs text-gray-400">
              <text class="truncate">工单：{{ item.workOrderNo || '-' }}</text>
            </view>
            <view v-if="item.plannedStartTime || item.plannedEndTime" class="mt-1 flex items-center text-xs text-gray-400">
              <text class="truncate">
                计划：{{ formatTime(item.plannedStartTime) || '-' }} ~ {{ formatTime(item.plannedEndTime) || '-' }}
              </text>
            </view>
            <view v-if="item.resName || item.teamName" class="mt-1 flex items-center text-xs text-gray-400">
              <text class="truncate">{{ item.resName || '-' }} · {{ item.teamName || '-' }}</text>
            </view>
          </view>
          <view class="ml-3 shrink-0">
            <wd-tag :type="dispatchStatusType(item.status)" size="small" variant="light">
              {{ dispatchStatusLabel(item.status) }}
            </wd-tag>
          </view>
        </view>
      </view>
    </z-paging>
  </view>
</template>

<style lang="scss" scoped>
.task-status-tabs {
  width: 100%;
}
</style>
