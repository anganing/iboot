<script lang="ts" setup>
import type { NotifyMessageItem } from '@/api/notify'
import { getMyNotifyMessagePage, getUnreadNotifyCount, updateAllNotifyMessageRead, updateNotifyMessageRead } from '@/api/notify'
import { tabbarStore } from '@/tabbar/store'

definePage({
  style: {
    navigationBarTitleText: '消息中心',
  },
})

/** 消息 tab 在自定义 tabbar 中的下标 */
const NOTIFY_TAB_INDEX = 1

const pagingRef = ref<ZPagingRef>()
const dataList = ref<NotifyMessageItem[]>([])
/** 0=全部 1=未读 */
const activeTab = ref(0)
const unreadCount = ref(0)

/** 刷新未读数并同步 tabbar 角标 */
async function refreshUnread() {
  try {
    const count = await getUnreadNotifyCount()
    unreadCount.value = count
    tabbarStore.setTabbarItemBadge(NOTIFY_TAB_INDEX, count > 0 ? count : undefined)
  }
  catch (error) {
    console.error('刷新未读数失败', error)
  }
}

async function queryList(pageNo: number, pageSize: number) {
  try {
    const res = await getMyNotifyMessagePage({
      pageNo,
      pageSize,
      readStatus: activeTab.value === 1 ? false : undefined,
    })
    pagingRef.value?.completeByTotal(res.list, res.total)
  }
  catch (error) {
    console.error('获取消息列表失败', error)
    pagingRef.value?.complete(false)
  }
}

// tab 切换后重置分页重新查询（用 watch 而非 change 事件：change 先于 v-model 更新触发，读到的是旧值）
watch(activeTab, () => {
  pagingRef.value?.reload()
})

async function onItemClick(item: NotifyMessageItem) {
  if (item.readStatus) {
    return
  }
  try {
    await updateNotifyMessageRead([item.id])
    item.readStatus = true
    refreshUnread()
  }
  catch (error) {
    console.error('标记已读失败', error)
  }
}

async function onReadAll() {
  try {
    await updateAllNotifyMessageRead()
    refreshUnread()
    pagingRef.value?.reload()
  }
  catch (error) {
    console.error('全部已读失败', error)
  }
}

onShow(() => {
  refreshUnread()
})
</script>

<template>
  <view class="h-full">
    <z-paging
      ref="pagingRef"
      v-model="dataList"
      bg-color="#f5f7fb"
      empty-view-text="暂无消息"
      safe-area-inset-bottom
      @query="queryList"
    >
      <template #top>
        <view class="flex items-center bg-white pr-4">
          <wd-tabs v-model="activeTab" custom-class="flex-1">
            <wd-tab title="全部" :name="0" />
            <wd-tab title="未读" :name="1" />
          </wd-tabs>
          <text v-if="unreadCount > 0" class="shrink-0 text-sm text-primary" @click="onReadAll">全部已读</text>
        </view>
      </template>

      <view
        v-for="item in dataList"
        :key="item.id"
        class="mx-4 mt-3 flex rounded-2xl bg-white p-4 shadow-sm"
        @click="onItemClick(item)"
      >
        <view class="h-10 w-10 center shrink-0 rounded-full bg-primary text-base text-white">
          {{ item.templateNickname.charAt(0) }}
        </view>
        <view class="ml-3 min-w-0 flex-1">
          <view class="flex items-center">
            <text class="truncate text-sm text-gray-800 font-semibold">{{ item.templateNickname }}</text>
            <view v-if="!item.readStatus" class="ml-2 h-2 w-2 shrink-0 rounded-full bg-[#ef4444]" />
            <text class="ml-auto shrink-0 pl-2 text-xs text-gray-400">{{ item.createTime }}</text>
          </view>
          <view class="content-clamp mt-1 text-sm text-gray-600">
            {{ item.templateContent }}
          </view>
        </view>
      </view>
    </z-paging>
  </view>
</template>

<style lang="scss" scoped>
.content-clamp {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  overflow: hidden;
}
</style>
