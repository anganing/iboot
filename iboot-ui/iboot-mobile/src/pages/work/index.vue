<script lang="ts" setup>
import type { MobileMenuNode } from '@/api/menu'
import { useMenuStore } from '@/store'

defineOptions({ name: 'Work' })

definePage({
  style: {
    navigationBarTitleText: '工作',
    navigationStyle: 'custom',
    enablePullDownRefresh: true,
  },
})

const ICON_ALIAS: Record<string, string> = { mail: 'email' }

function mapIcon(icon?: string) {
  return (icon && (ICON_ALIAS[icon] ?? icon)) || 'apps'
}

interface MenuSection {
  key: string
  title: string
  icon: string
  items: MobileMenuNode[]
}

function collectSection(node: MobileMenuNode, result: MenuSection[]) {
  const children = node.children || []
  const items = children.filter(item => item.type === 2 && item.path)
  if (items.length) {
    result.push({ key: `group-${node.id}`, title: node.name, icon: mapIcon(node.icon), items })
  }
  children.filter(item => item.type === 1).forEach(item => collectSection(item, result))
}

const menuStore = useMenuStore()
const sections = computed<MenuSection[]>(() => {
  const result: MenuSection[] = []
  menuStore.menus.filter(item => item.type === 1).forEach(item => collectSection(item, result))
  return result
})

function goPage(item: MobileMenuNode) {
  uni.navigateTo({
    url: item.path,
    fail: () => uni.showToast({ title: '页面暂不可用', icon: 'none' }),
  })
}

onShow(() => {
  menuStore.fetchMenu().catch(() => {})
})

onPullDownRefresh(async () => {
  try {
    await menuStore.fetchMenu(true)
  }
  finally {
    uni.stopPullDownRefresh()
  }
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb]">
    <view class="work-header px-5 pb-7 pt-safe">
      <view class="pt-5 text-2xl text-white font-bold">工作</view>
      <view class="mt-2 text-sm text-white/80">选择一个功能，开始处理今天的工作</view>
    </view>
    <view class="header-fade" />

    <view v-if="sections.length" class="px-4 pb-4">
      <view v-for="section in sections" :key="section.key" class="mb-6">
        <view class="mb-3 flex items-center px-1">
          <wd-icon :name="section.icon" size="20px" class="text-primary" />
          <text class="ml-2 text-base text-gray-800 font-semibold">{{ section.title }}</text>
        </view>
        <view class="grid grid-cols-4 gap-3">
          <view
            v-for="item in section.items"
            :key="item.id"
            class="flex flex-col items-center rounded-lg bg-white py-4 shadow-sm"
            @click="goPage(item)"
          >
            <wd-icon :name="mapIcon(item.icon)" size="26px" class="text-primary" />
            <text class="mt-2 max-w-full truncate px-1 text-xs text-gray-700">{{ item.name }}</text>
          </view>
        </view>
      </view>
    </view>

    <view v-else-if="menuStore.loaded" class="flex flex-col items-center pt-30">
      <wd-icon name="no-content" size="64px" class="text-gray-300" />
      <text class="mt-4 px-10 text-center text-sm text-gray-400">暂无可用功能，请联系管理员分配菜单权限</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.work-header {
  background: linear-gradient(180deg, #1c64fd 0%, #4d8bff 100%);
}

.header-fade {
  height: 48rpx;
  background: linear-gradient(180deg, #4d8bff 0%, #f5f7fb 100%);
}
</style>
