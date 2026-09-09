<script lang="ts" setup>
import { handleLoginSuccess } from '@/api/login'
import { useTokenStore } from '@/store/token'

definePage({
  style: {
    navigationBarTitleText: '登录',
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
  <view class="flex min-h-screen flex-col bg-white px-8">
    <view class="mt-20 text-center text-4xl font-bold">
      iboot
    </view>
    <view class="mt-2 text-center text-sm text-gray-400">
      欢迎登录
    </view>

    <wd-notice-bar
      v-if="captchaEnable"
      class="mt-8"
      text="移动端暂未接入滑块验证码，请管理员在后台关闭验证码后使用"
    />

    <view class="mt-10">
      <wd-input
        v-model="username"
        placeholder="请输入账号"
        clearable
        custom-class="h-12"
      />
      <wd-input
        v-model="password"
        show-password
        placeholder="请输入密码"
        custom-class="mt-4 h-12"
      />
    </view>

    <wd-button
      block
      class="mt-10"
      :disabled="captchaEnable"
      :loading="loading"
      @click="doLogin"
    >
      登录
    </wd-button>
  </view>
</template>

<style lang="scss" scoped>
//
</style>
