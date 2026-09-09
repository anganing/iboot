<script lang="ts" setup>
import { handleLoginSuccess } from '@/api/login'
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '登录',
    navigationStyle: 'custom',
    navigationBarTextStyle: 'white',
  },
})

const captchaEnable = import.meta.env.VITE_APP_CAPTCHA_ENABLE === 'true'

const tokenStore = useTokenStore()
const username = ref('')
const password = ref('')
const loading = ref(false)

onShow(() => {
  if (tokenStore.updateNowTime().hasLogin) {
    handleLoginSuccess()
  }
})

async function doLogin() {
  const name = username.value.trim()
  const pwd = password.value.trim()
  if (!name) {
    uni.showToast({ title: '请输入账号', icon: 'none' })
    return
  }
  if (!pwd) {
    uni.showToast({ title: '请输入密码', icon: 'none' })
    return
  }
  loading.value = true
  try {
    await tokenStore.login({ username: name, password: pwd })
    handleLoginSuccess()
  }
  catch (error) {
    console.error('登录失败', error)
  }
  finally {
    loading.value = false
  }
}
</script>

<template>
  <view class="login-page min-h-screen flex flex-col overflow-hidden">
    <!-- 顶部品牌区 -->
    <view class="login-header flex flex-col items-center px-8 pt-safe">
      <image src="/static/logo.svg" class="mt-20 h-20 w-20" />
      <view class="mt-6 text-4xl text-white font-bold">
        iboot
      </view>
      <view class="mt-2 text-sm text-white/70">
        智能制造运营平台
      </view>
    </view>

    <!-- 表单卡片 -->
    <view class="login-card mx-6 flex-1 rounded-3xl bg-white px-7 pb-10 pt-9 shadow-sm -mt-2">
      <wd-notice-bar
        v-if="captchaEnable"
        text="移动端暂未接入滑块验证码，请管理员在后台关闭验证码后使用"
      />

      <view class="mt-2 flex flex-col gap-5">
        <wd-input
          v-model="username"
          prefix-icon="user"
          placeholder="请输入账号"
          clearable
          custom-class="login-input"
          @confirm="doLogin"
        />
        <wd-input
          v-model="password"
          prefix-icon="lock"
          show-password
          placeholder="请输入密码"
          custom-class="login-input"
          @confirm="doLogin"
        />
      </view>

      <wd-button

        size="large"
        round block
        custom-class="mt-10"
        :disabled="captchaEnable"
        :loading="loading"
        @click="doLogin"
      >
        登 录
      </wd-button>

      <view class="mt-8 text-center text-xs text-gray-300">
        登录即代表同意平台使用条款
      </view>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.login-page {
  background: linear-gradient(180deg, #1c64fd 0%, #4d8bff 28%, #f5f7fb 100%);
}

.login-header {
  padding-bottom: 48rpx;
}

.login-card {
  :deep(.login-input) {
    background: #f5f7fb;
    border-radius: 16rpx;
  }
}
</style>
