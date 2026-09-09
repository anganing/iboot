<script lang="ts" setup>
import type { MobileMenuNode } from '@/api/menu'
import { getUnreadNotifyCount } from '@/api/notify'
import { useMenuStore, useUserStore } from '@/store'
import { tabbarStore } from '@/tabbar/store'

defineOptions({
  name: 'Home',
})

definePage({
  // 使用 type: "home" 属性设置首页
  type: 'home',
  style: {
    navigationBarTitleText: '首页',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

/** 消息中心入口的固定路径，用于挂未读角标 */
const NOTIFY_ENTRY_PATH = '/pages/notify/index'
/** 消息 tab 在自定义 tabbar 中的下标 */
const NOTIFY_TAB_INDEX = 1

/** 后端 icon 字典与 wd-icon 内置字形的差异映射 */
const ICON_ALIAS: Record<string, string> = { mail: 'email' }

function mapIcon(icon?: string) {
  return (icon && (ICON_ALIAS[icon] ?? icon)) || 'apps'
}

const menuStore = useMenuStore()
const userStore = useUserStore()
const nickname = computed(() => userStore.userInfo.nickname || '用户')

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

interface MenuSection {
  key: string
  title: string
  icon: string
  items: MobileMenuNode[]
}

/** 把分组树展开为渲染用的分组列表；分组内 type=2 且 path 非空的节点为宫格项，子分组递归展开 */
function collectSection(node: MobileMenuNode, result: MenuSection[]) {
  const children = node.children || []
  const items = children.filter(c => c.type === 2 && c.path)
  if (items.length) {
    result.push({ key: `group-${node.id}`, title: node.name, icon: mapIcon(node.icon), items })
  }
  children.filter(c => c.type === 1).forEach(c => collectSection(c, result))
}

const sections = computed<MenuSection[]>(() => {
  const result: MenuSection[] = []
  menuStore.menus.filter(n => n.type === 1).forEach(n => collectSection(n, result))
  return result
})

const unreadCount = ref(0)

/** 刷新未读数并同步 tabbar 角标；失败时 http 层已提示，这里静默 */
async function refreshUnread() {
  try {
    const count = await getUnreadNotifyCount()
    unreadCount.value = count
    tabbarStore.setTabbarItemBadge(NOTIFY_TAB_INDEX, count > 0 ? count : undefined)
  }
  catch {}
}

function badgeText(count: number) {
  return count > 99 ? '99+' : String(count)
}

function isNotifyEntry(node: MobileMenuNode) {
  return node.path === NOTIFY_ENTRY_PATH
}

function goPage(node: MobileMenuNode) {
  uni.navigateTo({
    url: node.path,
    fail: () => uni.showToast({ title: '页面暂不可用', icon: 'none' }),
  })
}

onShow(() => {
  menuStore.fetchMenu().catch(() => {})
  refreshUnread()
})

onPullDownRefresh(async () => {
  try {
    await menuStore.fetchMenu(true)
    await refreshUnread()
  }
  finally {
    uni.stopPullDownRefresh()
  }
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb]">
    <!-- 顶部品牌区 -->
    <view class="home-header px-5 pb-8 pt-safe">
      <view class="pt-6 text-2xl text-white font-bold">
        iboot 工作台
      </view>
      <view class="mt-2 text-sm text-white/80">
        {{ greeting }}，{{ nickname }}
      </view>
    </view>
    <view class="header-fade" />

    <!-- 菜单分组宫格 -->
    <view v-if="sections.length" class="px-4 pb-4">
      <view v-for="section in sections" :key="section.key" class="mb-2">
        <view class="mb-3 flex items-center px-1">
          <wd-icon :name="section.icon" size="20px" class="text-primary" />
          <text class="ml-2 text-base text-gray-800 font-semibold">{{ section.title }}</text>
        </view>
        <view class="grid grid-cols-4 gap-3">
          <view
            v-for="item in section.items"
            :key="item.id"
            class="relative flex flex-col items-center rounded-2xl bg-white py-4 shadow-sm"
            @click="goPage(item)"
          >
            <wd-icon :name="mapIcon(item.icon)" size="26px" class="text-primary" />
            <text class="mt-2 max-w-full truncate px-1 text-xs text-gray-700">{{ item.name }}</text>
            <view
              v-if="isNotifyEntry(item) && unreadCount > 0"
              class="absolute right-2 top-2 h-4 min-w-4 flex items-center justify-center rounded-full bg-[#ef4444] px-1 text-3xs text-white"
            >
              {{ badgeText(unreadCount) }}
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 空态 -->
    <view v-else-if="menuStore.loaded" class="flex flex-col items-center pt-30">
      <wd-icon name="no-content" size="64px" class="text-gray-300" />
      <text class="mt-4 px-10 text-center text-sm text-gray-400">暂无可用功能，请联系管理员分配菜单权限</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.home-header {
  background: linear-gradient(180deg, #1c64fd 0%, #4d8bff 100%);
}

.header-fade {
  height: 48rpx;
  background: linear-gradient(180deg, #4d8bff 0%, #f5f7fb 100%);
}
</style>
