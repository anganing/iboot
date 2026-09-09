<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '我的',
  },
})

const userStore = useUserStore()
const tokenStore = useTokenStore()
const { userInfo } = storeToRefs(userStore)

const DEFAULT_AVATAR = '/static/images/default-avatar.png'
const displayName = computed(() => userInfo.value.nickname || userInfo.value.username)
const subTitle = computed(() => {
  const { deptName, username } = userInfo.value
  return deptName ? `${deptName} · ${username}` : username
})

onShow(() => {
  tokenStore.updateNowTime()
})

function goLogin() {
  uni.navigateTo({ url: LOGIN_PAGE })
}

function toPage(url: string) {
  if (!tokenStore.updateNowTime().hasLogin) {
    goLogin()
    return
  }
  uni.navigateTo({ url })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (res.confirm) {
        await tokenStore.logout()
        uni.showToast({ title: '已退出登录', icon: 'success' })
      }
    },
  })
}
</script>

<template>
  <view class="flex min-h-screen flex-col bg-gray-100">
    <view
      class="flex items-center bg-white px-6 pb-8 pt-10"
      @click="!tokenStore.hasLogin && goLogin()"
    >
      <image
        class="h-16 w-16 rounded-full bg-gray-200"
        :src="userInfo.avatar || DEFAULT_AVATAR"
        mode="aspectFill"
      />
      <view class="ml-4 flex flex-col flex-1 overflow-hidden">
        <template v-if="tokenStore.hasLogin">
          <text class="truncate text-xl font-bold">
            {{ displayName }}
          </text>
          <text class="mt-1 truncate text-sm text-gray-500">
            {{ subTitle }}
          </text>
        </template>
        <template v-else>
          <text class="text-xl font-bold">
            点击登录
          </text>
          <text class="mt-1 text-sm text-gray-500">
            登录后可使用完整功能
          </text>
        </template>
      </view>
    </view>

    <wd-cell-group class="mt-3" border>
      <wd-cell title="个人资料" is-link @click="toPage('/pages/me/profile')" />
      <wd-cell title="修改密码" is-link @click="toPage('/pages/me/password')" />
    </wd-cell-group>

    <view v-if="tokenStore.hasLogin" class="mt-10 px-6">
      <wd-button block type="danger" @click="handleLogout">
        退出登录
      </wd-button>
    </view>
  </view>
</template>
