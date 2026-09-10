<script lang="ts" setup>
import { getTaskTodoCount } from '@/api/bpm'
import { getUnreadNotifyCount } from '@/api/notify'
import { useUserStore } from '@/store'
import { tabbarStore } from '@/tabbar/store'

defineOptions({ name: 'Home' })

definePage({
  type: 'home',
  style: {
    navigationBarTitleText: '首页',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const MESSAGE_TAB_INDEX = 2
const userStore = useUserStore()
const nickname = computed(() => userStore.userInfo.nickname || '用户')
const todoCount = ref(0)
const notifyCount = ref(0)

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6)
    return '夜深了'
  if (hour < 12)
    return '上午好'
  if (hour < 14)
    return '中午好'
  if (hour < 18)
    return '下午好'
  return '晚上好'
})

const messageBadge = computed(() => todoCount.value + notifyCount.value)

async function refreshOverview() {
  const [todoResult, notifyResult] = await Promise.allSettled([
    getTaskTodoCount(),
    getUnreadNotifyCount(),
  ])
  todoCount.value = todoResult.status === 'fulfilled' ? todoResult.value : 0
  notifyCount.value = notifyResult.status === 'fulfilled' ? notifyResult.value : 0
  tabbarStore.setTabbarItemBadge(MESSAGE_TAB_INDEX, messageBadge.value || undefined)
}

function goMessage() {
  uni.switchTab({ url: '/pages/notify/index' })
}

function goWork() {
  uni.switchTab({ url: '/pages/work/index' })
}

onShow(() => {
  refreshOverview()
})

onPullDownRefresh(async () => {
  try {
    await refreshOverview()
  }
  finally {
    uni.stopPullDownRefresh()
  }
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb]">
    <view class="home-header px-5 pb-12 pt-safe">
      <view class="pt-7 text-2xl text-white font-bold">{{ greeting }}，{{ nickname }}</view>
      <view class="mt-2 text-sm text-white/80">从待办和消息开始今天的工作</view>
    </view>

    <view class="mx-4 -mt-7">
      <view class="rounded-lg bg-white p-4 shadow-sm" @click="goMessage">
        <view class="flex items-center">
          <view class="h-11 w-11 center rounded-lg bg-[#e8f0ff] text-primary">
            <wd-icon name="task" size="24px" />
          </view>
          <view class="ml-3 min-w-0 flex-1">
            <text class="block text-base text-gray-800 font-semibold">待我处理</text>
            <text class="mt-1 block text-xs text-gray-500">及时处理流程任务</text>
          </view>
          <view class="flex items-center">
            <text class="mr-2 text-2xl text-gray-800 font-semibold">{{ todoCount }}</text>
            <wd-icon name="arrow-right" class="text-gray-300" />
          </view>
        </view>
      </view>

      <view class="mt-3 rounded-lg bg-white p-4 shadow-sm" @click="goMessage">
        <view class="flex items-center">
          <view class="h-11 w-11 center rounded-lg bg-[#eaf8f1] text-[#16a36a]">
            <wd-icon name="notification" size="24px" />
          </view>
          <view class="ml-3 min-w-0 flex-1">
            <text class="block text-base text-gray-800 font-semibold">通知消息</text>
            <text class="mt-1 block text-xs text-gray-500">查看系统通知和站内信</text>
          </view>
          <view class="flex items-center">
            <text class="mr-2 text-2xl text-gray-800 font-semibold">{{ notifyCount }}</text>
            <wd-icon name="arrow-right" class="text-gray-300" />
          </view>
        </view>
      </view>
    </view>

    <view class="mx-4 mt-7 flex items-center justify-between">
      <text class="text-base text-gray-800 font-semibold">常用工作</text>
      <text class="text-sm text-primary" @click="goWork">全部功能</text>
    </view>
    <view class="mx-4 mt-3 rounded-lg bg-white p-4 shadow-sm" @click="goWork">
      <view class="flex items-center">
        <view class="h-10 w-10 center rounded-lg bg-[#fff4e8] text-[#e68228]">
          <wd-icon name="apps" size="22px" />
        </view>
        <view class="ml-3 flex-1">
          <text class="block text-sm text-gray-800 font-medium">进入工作台</text>
          <text class="mt-1 block text-xs text-gray-500">生产、仓储及更多业务功能</text>
        </view>
        <wd-icon name="arrow-right" class="text-gray-300" />
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.home-header {
  background: linear-gradient(180deg, #1c64fd 0%, #4d8bff 100%);
}
</style>
