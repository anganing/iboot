<script lang="ts" setup>
definePage({
  style: {
    navigationBarTitleText: '功能详情',
  },
})

const title = ref('')

onLoad((options) => {
  title.value = options?.title ? decodeURIComponent(options.title) : ''
  if (title.value) {
    uni.setNavigationBarTitle({ title: title.value })
  }
})
</script>

<template>
  <view class="min-h-screen flex flex-col items-center bg-[#f5f7fb] pt-40">
    <wd-icon name="settings" size="64px" class="text-gray-300" />
    <text class="mt-4 px-10 text-center text-sm text-gray-400">
      「{{ title || '该功能' }}」功能开发中，敬请期待
    </text>
  </view>
</template>
