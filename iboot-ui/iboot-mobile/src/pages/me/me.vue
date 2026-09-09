<script lang="ts" setup>
import { storeToRefs } from 'pinia'
import { LOGIN_PAGE } from '@/router/config'
import { useUserStore } from '@/store'
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '我的',
    navigationStyle: 'custom',
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
        // 退出后直接回到登录页
        setTimeout(() => {
          uni.reLaunch({ url: LOGIN_PAGE })
        }, 600)
      }
    },
  })
}
</script>

<template>
  <view class="min-h-screen flex flex-col bg-[#f5f7fb]">
    <!-- 头部：渐变背景 + 用户卡片压边 -->
    <view class="me-header px-6 pt-safe">
      <view class="pb-20 pt-12 text-center text-2xl text-white font-bold">
        个人中心
      </view>
    </view>
    <view
      class="user-card mx-4 flex items-center rounded-2xl bg-white px-6 py-6 shadow-sm -mt-16"
      @click="!tokenStore.hasLogin && goLogin()"
    >
      <image
        class="h-16 w-16 shrink-0 rounded-full bg-gray-100"
        :src="userInfo.avatar || DEFAULT_AVATAR"
        mode="aspectFill"
      />
      <view class="ml-4 flex flex-1 flex-col overflow-hidden">
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
          <text class="mt-1 text-sm text-gray-400">
            登录后可使用完整功能
          </text>
        </template>
      </view>
      <wd-icon v-if="!tokenStore.hasLogin" name="arrow-right" class="text-gray-300" />
    </view>

    <!-- 菜单 -->
    <wd-cell-group custom-class="mx-4 mt-4 rounded-2xl overflow-hidden">
      <wd-cell
        title="个人资料"
        prefix-icon="user"
        is-link
        clickable
        @click="toPage('/pages/me/profile')"
      />
      <wd-cell
        title="修改密码"
        prefix-icon="lock"
        is-link
        clickable
        @click="toPage('/pages/me/password')"
      />
    </wd-cell-group>

    <view v-if="tokenStore.hasLogin" class="mt-10 px-4 pb-safe">
      <wd-button round block type="danger" variant="plain" custom-class="logout-btn" @click="handleLogout">
        退出登录
      </wd-button>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.me-header {
  background: linear-gradient(180deg, #1c64fd 0%, #4d8bff 100%);
}

.logout-btn {
  border-color: #fecaca;
  color: #ef4444;
}
</style>
