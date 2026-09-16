<script lang="ts" setup>
import type { EsopFile, EsopStep } from '@/api/mdm/esop'
import { getEsopFileIcon, getEsopListByRouteOp, isImageFile } from '@/api/mdm/esop'

defineOptions({ name: 'MesTaskEsop' })

definePage({
  style: {
    navigationBarTitleText: '作业指导书',
  },
})

/**
 * 路线工序编号用字符串承接，**不要 `Number()`**——后端主键是雪花 ID（19 位），
 * 超出 JS 安全整数范围，转 number 会丢精度导致查不到数据。
 */
const routeOpId = ref('')
const steps = ref<EsopStep[]>([])
const loading = ref(true)
/** 当前工序下所有图片附件，供 previewImage 一次传入、左右滑动 */
const imageUrls = computed(() =>
  steps.value.flatMap(step => (step.files ?? []).filter(isImageFile).map(file => file.fileUrl)),
)

/**
 * 解析工艺参数 JSON。
 *
 * paramJson 是后端存的字符串，内容不受控——解析失败必须兜住返回空数组，
 * 不能让一 malformed 的 JSON 把整个页面打成白屏。
 */
function parseParams(paramJson?: string): { label: string, value: string }[] {
  if (!paramJson)
    return []
  try {
    const parsed = JSON.parse(paramJson)
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed))
      return []
    return Object.entries(parsed as Record<string, unknown>)
      .filter(([, value]) => value !== null && value !== undefined)
      .map(([label, value]) => ({ label, value: String(value) }))
  }
  catch {
    return []
  }
}

function previewImage(file: EsopFile) {
  const urls = imageUrls.value
  if (!urls.length)
    return
  uni.previewImage({ current: file.fileUrl, urls })
}

/**
 * 打开非图片附件。
 *
 * H5 直接新开窗口；小程序端先下载到本地再交给系统打开（小程序没有直接打开远程文件的能力）。
 * fileUrl 是 24 小时过期的预签名地址，每次进页面重新取，不缓存。
 */
function openFile(file: EsopFile) {
  // #ifdef H5
  window.open(file.fileUrl)
  // #endif
  // #ifndef H5
  uni.showLoading({ title: '正在打开' })
  uni.downloadFile({
    url: file.fileUrl,
    success: (res) => {
      if (res.statusCode !== 200) {
        uni.showToast({ icon: 'none', title: '文件下载失败' })
        return
      }
      uni.openDocument({
        filePath: res.tempFilePath,
        showMenu: true,
        fail: () => uni.showToast({ icon: 'none', title: '该文件类型暂不支持预览' }),
      })
    },
    fail: () => uni.showToast({ icon: 'none', title: '文件下载失败' }),
    complete: () => uni.hideLoading(),
  })
  // #endif
}

async function loadSteps() {
  if (!routeOpId.value)
    return
  loading.value = true
  try {
    const list = await getEsopListByRouteOp(routeOpId.value)
    // 后端已按 seq 排序，这里再兜一次，避免后端调整后移动端顺序错乱
    steps.value = [...(list ?? [])].sort((a, b) => (a.seq ?? 0) - (b.seq ?? 0))
  }
  catch (error) {
    console.error('获取作业指导书失败', error)
  }
  finally {
    loading.value = false
  }
}

onLoad((options) => {
  routeOpId.value = options?.routeOpId ? String(options.routeOpId) : ''
  loadSteps()
})
</script>

<template>
  <view class="min-h-screen bg-[#f5f7fb] p-4 pb-safe">
    <view v-if="loading" class="pt-20 text-center text-sm text-gray-400">
      加载中...
    </view>

    <template v-else-if="steps.length">
      <view
        v-for="(step, index) in steps"
        :key="step.id"
        class="mb-3 rounded-lg bg-white p-4 shadow-sm"
      >
        <view class="flex items-start">
          <view class="mr-3 h-6 w-6 center shrink-0 rounded-full bg-[#e8f0ff] text-xs text-primary font-semibold">
            {{ index + 1 }}
          </view>
          <view class="min-w-0 flex-1">
            <text class="block text-sm text-gray-900 font-semibold">{{ step.title }}</text>
            <view v-if="step.requiredConfirmFlag" class="mt-1">
              <wd-tag type="warning" size="small" variant="light">
                需确认阅读
              </wd-tag>
            </view>
          </view>
        </view>

        <text v-if="step.content" class="mt-3 block text-sm text-gray-600 leading-relaxed">
          {{ step.content }}
        </text>

        <!-- 工艺参数：JSON 解析失败时 parseParams 返回空数组，整块不渲染，不会白屏 -->
        <view v-if="parseParams(step.paramJson).length" class="mt-3 rounded-lg bg-[#f7f9fc] p-3">
          <text class="mb-2 block text-2xs text-gray-400">工艺参数</text>
          <view
            v-for="param in parseParams(step.paramJson)"
            :key="param.label"
            class="flex items-baseline py-0.5"
          >
            <text class="w-24 shrink-0 text-xs text-gray-400">{{ param.label }}</text>
            <text class="min-w-0 flex-1 break-all text-xs text-gray-700">{{ param.value }}</text>
          </view>
        </view>

        <!-- 附件：图片走缩略图网格（点击 previewImage），其余按类型给图标 + 文件名 -->
        <view v-if="step.files && step.files.length" class="mt-3">
          <text class="mb-2 block text-2xs text-gray-400">附件 {{ step.files.length }}</text>
          <view class="grid grid-cols-3 gap-2">
            <template v-for="file in step.files" :key="file.id">
              <view
                v-if="isImageFile(file)"
                class="aspect-square overflow-hidden rounded-lg bg-[#f0f2f5]"
                @click="previewImage(file)"
              >
                <image :src="file.fileUrl" mode="aspectFill" class="h-full w-full" />
              </view>
              <view
                v-else
                class="flex items-center rounded-lg bg-[#f7f9fc] px-3 py-3"
                @click="openFile(file)"
              >
                <wd-icon :name="getEsopFileIcon(file.fileType)" size="20px" class="shrink-0 text-primary" />
                <text class="ml-2 min-w-0 flex-1 truncate text-xs text-gray-700">{{ file.fileName }}</text>
              </view>
            </template>
          </view>
        </view>
      </view>
    </template>

    <view v-else class="flex flex-col items-center pt-20">
      <wd-icon name="no-content" size="64px" class="text-gray-300" />
      <text class="mt-4 px-10 text-center text-sm text-gray-400">该工序未配置作业指导书</text>
    </view>
  </view>
</template>
