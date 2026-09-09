<script lang="ts" setup>
import { updateUserPassword } from '@/api/login'

definePage({
  style: {
    navigationBarTitleText: '修改密码',
  },
})

const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const saving = ref(false)

function validate(): string {
  const oldPwd = oldPassword.value.trim()
  const newPwd = newPassword.value.trim()
  const confirmPwd = confirmPassword.value.trim()
  if (!oldPwd) {
    return '请输入旧密码'
  }
  if (!newPwd) {
    return '请输入新密码'
  }
  if (!confirmPwd) {
    return '请再次输入新密码'
  }
  if (oldPwd.length < 4 || oldPwd.length > 16) {
    return '旧密码长度为 4-16 位'
  }
  if (newPwd.length < 4 || newPwd.length > 16) {
    return '新密码长度为 4-16 位'
  }
  if (newPwd !== confirmPwd) {
    return '两次输入的新密码不一致'
  }
  return ''
}

async function handleSave() {
  const msg = validate()
  if (msg) {
    uni.showToast({ title: msg, icon: 'none' })
    return
  }
  saving.value = true
  try {
    await updateUserPassword({ oldPassword: oldPassword.value.trim(), newPassword: newPassword.value.trim() })
    uni.showToast({ title: '密码修改成功', icon: 'success' })
    uni.navigateBack()
  }
  catch (error) {
    console.error('修改密码失败', error)
  }
  finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="min-h-screen flex flex-col bg-[#f5f7fb]">
    <wd-cell-group custom-class="mx-4 mt-4 rounded-2xl overflow-hidden" border>
      <wd-cell title="旧密码" required center>
        <wd-input
          v-model="oldPassword"
          show-password
          compact
          placeholder="请输入旧密码"
          custom-class="text-right"
        />
      </wd-cell>
      <wd-cell title="新密码" required center>
        <wd-input
          v-model="newPassword"
          show-password
          compact
          placeholder="4-16 位"
          custom-class="text-right"
        />
      </wd-cell>
      <wd-cell title="确认新密码" required center>
        <wd-input
          v-model="confirmPassword"
          show-password
          compact
          placeholder="请再次输入新密码"
          custom-class="text-right"
        />
      </wd-cell>
    </wd-cell-group>

    <view class="mt-10 px-4 pb-safe">
      <wd-button round block size="large" :loading="saving" @click="handleSave">
        保存
      </wd-button>
    </view>
  </view>
</template>
