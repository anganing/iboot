<script lang="ts" setup>
import type { BpmCopyItem, BpmTaskItem } from '@/api/bpm'
import type { NotifyMessageItem } from '@/api/notify'
import { getProcessInstanceCopyPage, getTaskDonePage, getTaskTodoCount, getTaskTodoPage } from '@/api/bpm'
import { getMyNotifyMessagePage, getUnreadNotifyCount, updateAllNotifyMessageRead, updateNotifyMessageRead } from '@/api/notify'
import { tabbarStore } from '@/tabbar/store'

defineOptions({ name: 'Notify' })

definePage({
  style: {
    navigationBarTitleText: '消息',
    navigationStyle: 'custom',
  },
})

type MessageTab = 'todo' | 'done' | 'copy' | 'notify'
type MessageItem = BpmCopyItem | BpmTaskItem | NotifyMessageItem

const MESSAGE_TAB_INDEX = 2
const tabs: Array<{ name: MessageTab, title: string }> = [
  { name: 'todo', title: '待办' },
  { name: 'done', title: '已办' },
  { name: 'copy', title: '抄送' },
  { name: 'notify', title: '通知' },
]

const pagingRef = ref<ZPagingRef>()
const dataList = ref<MessageItem[]>([])
const activeTab = ref<MessageTab>('todo')
const todoCount = ref(0)
const notifyCount = ref(0)
const selectedNotify = ref<NotifyMessageItem>()
const showNotifyDetail = ref(false)
const todoTabTitle = computed(() => todoCount.value > 0 ? `待办 ${todoCount.value}` : '待办')
const messageBadge = computed(() => todoCount.value + notifyCount.value)

function isNotifyItem(item: MessageItem): item is NotifyMessageItem {
  return 'templateContent' in item
}

function isCopyItem(item: MessageItem): item is BpmCopyItem {
  return 'processInstanceName' in item
}

function getTitle(item: MessageItem) {
  if (isNotifyItem(item))
    return item.templateNickname
  if (isCopyItem(item))
    return item.processInstanceName
  return item.processInstance?.name || item.name
}

function getSubtitle(item: MessageItem) {
  if (isNotifyItem(item))
    return getPlainText(item.templateContent)
  if (isCopyItem(item))
    return item.activityName || '流程抄送'
  return item.name
}

function getTime(item: MessageItem) {
  if (isNotifyItem(item) || isCopyItem(item))
    return formatTime(item.createTime)
  return formatTime(item.endTime || item.createTime)
}

function getPlainText(content: string) {
  return content
    .replace(/<[^>]*>/g, ' ')
    .replace(/&nbsp;/gi, ' ')
    .replace(/&amp;/gi, '&')
    .replace(/&lt;/gi, '<')
    .replace(/&gt;/gi, '>')
    .replace(/\s+/g, ' ')
    .trim()
}

/**
 * rich-text 会保留后台富文本的内联宽高，移动端容器会因此被撑出横向滚动。
 * 通知仅用于阅读，去除排版尺寸后由页面统一控制宽度，图片保持自适应。
 */
function getMobileRichText(content: string) {
  return content
    .replace(/\s(?:width|height)=(['"]).*?\1/gi, '')
    .replace(/\sstyle=(['"]).*?\1/gi, '')
    .replace(/<img\b/gi, '<img style="max-width:100%;height:auto;display:block;"')
}

function formatTime(value?: number | string) {
  if (!value)
    return ''
  const timestamp = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime()))
    return String(value)
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

function getSummary(item: MessageItem) {
  if (isNotifyItem(item))
    return ''
  const summary = isCopyItem(item) ? item.summary : item.processInstance?.summary
  return summary?.map(pair => `${pair.key}：${pair.value}`).join(' · ') || ''
}

async function refreshCounts() {
  const [todoResult, notifyResult] = await Promise.allSettled([
    getTaskTodoCount(),
    getUnreadNotifyCount(),
  ])
  todoCount.value = todoResult.status === 'fulfilled' ? todoResult.value : 0
  notifyCount.value = notifyResult.status === 'fulfilled' ? notifyResult.value : 0
  tabbarStore.setTabbarItemBadge(MESSAGE_TAB_INDEX, messageBadge.value || undefined)
}

async function queryList(pageNo: number, pageSize: number) {
  try {
    if (activeTab.value === 'todo') {
      const result = await getTaskTodoPage({ pageNo, pageSize })
      pagingRef.value?.completeByTotal(result.list, result.total)
      return
    }
    if (activeTab.value === 'done') {
      const result = await getTaskDonePage({ pageNo, pageSize })
      pagingRef.value?.completeByTotal(result.list, result.total)
      return
    }
    if (activeTab.value === 'copy') {
      const result = await getProcessInstanceCopyPage({ pageNo, pageSize })
      pagingRef.value?.completeByTotal(result.list, result.total)
      return
    }
    const result = await getMyNotifyMessagePage({ pageNo, pageSize })
    pagingRef.value?.completeByTotal(result.list, result.total)
  }
  catch (error) {
    console.error('获取消息列表失败', error)
    pagingRef.value?.complete(false)
  }
}

function selectTab(tab: MessageTab) {
  if (activeTab.value !== tab)
    activeTab.value = tab
}

watch(activeTab, () => {
  pagingRef.value?.reload()
})

async function onItemClick(item: MessageItem) {
  if (isNotifyItem(item)) {
    if (!item.readStatus) {
      await updateNotifyMessageRead([item.id])
      item.readStatus = true
      await refreshCounts()
    }
    selectedNotify.value = item
    showNotifyDetail.value = true
    return
  }
  const processInstanceId = item.processInstanceId
  const taskId = isCopyItem(item) ? item.taskId : item.id
  const activityId = isCopyItem(item) ? item.activityId : undefined
  const query = [
    `processInstanceId=${encodeURIComponent(processInstanceId)}`,
    taskId ? `taskId=${encodeURIComponent(taskId)}` : '',
    activityId ? `activityId=${encodeURIComponent(activityId)}` : '',
  ].filter(Boolean).join('&')
  uni.navigateTo({ url: `/pages/process/detail?${query}` })
}

async function onReadAll() {
  try {
    await updateAllNotifyMessageRead()
    await refreshCounts()
    pagingRef.value?.reload()
  }
  catch (error) {
    console.error('全部标记已读失败', error)
  }
}

onShow(() => {
  refreshCounts()
})
</script>

<template>
  <view class="h-full bg-[#f5f7fb]">
    <z-paging ref="pagingRef" v-model="dataList" :default-page-size="5" bg-color="#f5f7fb" :empty-view-text="activeTab === 'todo' ? '暂无待办任务' : '暂无消息'" safe-area-inset-bottom @query="queryList">
      <template #top>
        <view class="bg-white px-4 pb-3 pt-safe">
          <view class="pt-3 text-xl text-gray-900 font-bold">消息</view>
          <view class="mt-4 flex rounded-lg bg-[#f1f4f8] p-1">
            <view v-for="tab in tabs" :key="tab.name" class="min-w-0 flex flex-1 items-center justify-center rounded-md py-2 text-sm" :class="activeTab === tab.name ? 'bg-white text-primary font-semibold shadow-sm' : 'text-gray-500'" @click="selectTab(tab.name)">
              <text class="truncate">{{ tab.name === 'todo' ? todoTabTitle : tab.title }}</text>
            </view>
          </view>
          <view v-if="activeTab === 'notify' && notifyCount > 0" class="mt-3 text-right">
            <text class="text-sm text-primary" @click="onReadAll">全部标记已读</text>
          </view>
        </view>
      </template>

      <view v-for="item in dataList" :key="`${activeTab}-${item.id}`" class="mx-4 mt-3 rounded-lg bg-white p-4 shadow-sm" @click="onItemClick(item)">
        <view class="flex items-start">
          <view class="h-10 w-10 center shrink-0 rounded-lg" :class="activeTab === 'todo' ? 'bg-[#e8f0ff] text-primary' : activeTab === 'done' ? 'bg-[#eaf8f1] text-[#16a36a]' : activeTab === 'copy' ? 'bg-[#fff3e8] text-[#e68228]' : 'bg-[var(--app-primary-soft)] text-primary'">
            <wd-icon :name="activeTab === 'notify' ? 'notification' : 'task'" size="22px" />
          </view>
          <view class="ml-3 min-w-0 flex-1">
            <view class="flex items-center">
              <text class="truncate text-sm text-gray-800 font-semibold">{{ getTitle(item) }}</text>
              <view v-if="isNotifyItem(item) && !item.readStatus" class="ml-2 h-2 w-2 shrink-0 rounded-full bg-[#ef4444]" />
              <text class="ml-auto shrink-0 pl-2 text-xs text-gray-400">{{ getTime(item) }}</text>
            </view>
            <text class="content-clamp mt-2 block text-sm text-gray-600">{{ getSubtitle(item) }}</text>
            <text v-if="getSummary(item)" class="content-clamp mt-2 block text-xs text-gray-400">{{ getSummary(item) }}</text>
          </view>
        </view>
      </view>
    </z-paging>

    <wd-popup v-model="showNotifyDetail" position="bottom" round safe-area-inset-bottom :z-index="1100" custom-class="notify-detail-popup">
      <view v-if="selectedNotify" class="h-[70vh] flex flex-col bg-white">
        <view class="flex items-center justify-between px-5 py-4">
          <text class="text-lg text-gray-900 font-semibold">{{ selectedNotify.templateNickname || '通知详情' }}</text>
          <wd-icon name="close" size="20px" class="text-gray-400" @click="showNotifyDetail = false" />
        </view>
        <scroll-view scroll-y :scroll-x="false" class="notify-detail-scroll">
          <view class="notify-detail-content">
            <text class="block text-xs text-gray-400">{{ formatTime(selectedNotify.createTime) }}</text>
            <view class="my-4 h-px bg-[#edf0f4]" />
            <rich-text :nodes="getMobileRichText(selectedNotify.templateContent)" class="notify-rich-text" />
          </view>
        </scroll-view>
      </view>
    </wd-popup>
  </view>
</template>

<style lang="scss" scoped>
.content-clamp {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

:deep(.notify-detail-popup) {
  z-index: 1100;
}

.notify-rich-text {
  display: block;
  max-width: 100%;
  color: #374151;
  font-size: 30rpx;
  line-height: 1.75;
  word-break: break-all;
}

.notify-detail-scroll {
  flex: 1;
  width: 100%;
  height: 0;
  min-width: 0;
  min-height: 0;
  box-sizing: border-box;
}

.notify-detail-content {
  width: 100%;
  min-width: 0;
  box-sizing: border-box;
  padding: 0 20px 24px;
  white-space: normal;
  overflow-wrap: anywhere;
}
</style>
