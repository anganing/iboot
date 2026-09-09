<script lang="ts" setup>
import type { IUpdateInfo } from '@/api/types/login'
import { storeToRefs } from 'pinia'
import { updateInfo } from '@/api/login'
import { useUserStore } from '@/store'

definePage({
  style: {
    navigationBarTitleText: '个人资料',
  },
})

const userStore = useUserStore()
const { userInfo } = storeToRefs(userStore)

const form = reactive({
  nickname: '',
  mobile: '',
  email: '',
  sex: 0,
})
const saving = ref(false)
const pickerRef = ref()

const sexColumns = [
  { label: '未知', value: 0 },
  { label: '男', value: 1 },
  { label: '女', value: 2 },
]

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
  }
  finally {
    form.nickname = userInfo.value.nickname || ''
    form.mobile = userInfo.value.mobile || ''
    form.email = userInfo.value.email || ''
    form.sex = userInfo.value.sex ?? 0
  }
})

const sexLabel = computed(() => sexColumns.find(c => c.value === form.sex)?.label || '未知')

// wd-picker 的 modelValue 是 (string|number)[]，用计算属性与 form.sex 双向桥接
const sexValue = computed({
  get: () => [form.sex],
  set: (vals) => {
    form.sex = Number(vals[0] ?? 0)
  },
})

function validate(): string {
  const nickname = form.nickname.trim()
  if (!nickname) {
    return '请输入昵称'
  }
  if (nickname.length > 30) {
    return '昵称不能超过 30 个字符'
  }
  if (form.mobile && !/^\d{11}$/.test(form.mobile)) {
    return '手机号必须为 11 位数字'
  }
  if (form.email && !/^[^\s@]+@[^\s@][^\s.@]*\.[^\s@]+$/.test(form.email)) {
    return '邮箱格式不正确'
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
    // 空字符串字段不提交，后端 mobile/email 带 @Length/@Email 校验，空串会 400
    const payload: IUpdateInfo = {}
    if (form.nickname.trim()) {
      payload.nickname = form.nickname.trim()
    }
    if (form.mobile) {
      payload.mobile = form.mobile
    }
    if (form.email) {
      payload.email = form.email
    }
    payload.sex = form.sex
    await updateInfo(payload)
    uni.showToast({ title: '保存成功', icon: 'success' })
    await userStore.fetchUserInfo()
    uni.navigateBack()
  }
  catch (error) {
    console.error('保存个人资料失败', error)
  }
  finally {
    saving.value = false
  }
}
</script>

<template>
  <view class="min-h-screen flex flex-col bg-[#f5f7fb]">
    <wd-cell-group custom-class="mx-4 mt-4 rounded-2xl overflow-hidden" border>
      <wd-cell title="昵称" required center>
        <wd-input
          v-model="form.nickname"
          compact
          placeholder="请输入昵称"
          :maxlength="30"
          custom-class="text-right"
        />
      </wd-cell>
      <wd-cell title="手机号" center>
        <wd-input
          v-model="form.mobile"
          type="number"
          compact
          placeholder="选填"
          :maxlength="11"
          custom-class="text-right"
        />
      </wd-cell>
      <wd-cell title="邮箱" center>
        <wd-input
          v-model="form.email"
          compact
          placeholder="选填"
          custom-class="text-right"
        />
      </wd-cell>
      <wd-cell
        title="性别"
        :value="sexLabel"
        is-link
        @click="pickerRef?.open()"
      />
    </wd-cell-group>

    <wd-picker
      ref="pickerRef"
      v-model="sexValue"
      title="选择性别"
      :columns="sexColumns"
    />

    <view class="mt-10 px-4 pb-safe">
      <wd-button round block size="large" :loading="saving" @click="handleSave">
        保存
      </wd-button>
    </view>
  </view>
</template>
