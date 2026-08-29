<script lang="ts" setup>
import type { MdmRouteOpEsopApi } from '#/api/mdm/routeopesop';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { FileViewer } from '@file-viewer/vue3';
import { ElMessage } from 'element-plus';

import { fileViewerOptions } from '#/components/file-viewer/options';

import '@file-viewer/vue3/dist/file-viewer3.css';

defineOptions({ name: 'RouteOpEsopPreview' });

interface EsopPreviewData {
  files?: MdmRouteOpEsopApi.RouteOpEsopFile[];
  title?: string;
}

const files = ref<MdmRouteOpEsopApi.RouteOpEsopFile[]>([]);
const activeIndex = ref(0);
const previewFile = ref<File>();
const previewTitle = ref('作业步骤附件预览');
const loading = ref(false);

/**
 * 拉取文件内容后交给 FileViewer 渲染
 * 附件地址是文件存储器签发的预签名地址，签名只覆盖 host，
 * 必须用不带 Authorization 的原生 fetch 直取，否则存储器会以签名不匹配拒绝
 */
async function loadPreview(file: MdmRouteOpEsopApi.RouteOpEsopFile) {
  if (!file?.fileUrl) {
    previewFile.value = undefined;
    return;
  }
  loading.value = true;
  try {
    const response = await fetch(file.fileUrl);
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`);
    }
    const blob = await response.blob();
    previewFile.value = new File([blob], file.fileName || 'esop-file', {
      type: blob.type,
    });
  } catch {
    ElMessage.error('文件加载失败，请检查文件是否存在或稍后重试');
    previewFile.value = undefined;
  } finally {
    loading.value = false;
  }
}

/** 切换左侧附件 */
async function handleSelect(index: number) {
  activeIndex.value = index;
  await loadPreview(files.value[index]!);
}

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      const data = modalApi.getData<EsopPreviewData>();
      files.value = data?.files ?? [];
      previewTitle.value = data?.title
        ? `${data.title} - 附件预览`
        : '作业步骤附件预览';
      activeIndex.value = 0;
      if (files.value.length > 0) {
        void loadPreview(files.value[0]!);
      }
    } else {
      files.value = [];
      previewFile.value = undefined;
      previewTitle.value = '作业步骤附件预览';
    }
  },
});
</script>

<template>
  <Modal :title="previewTitle" class="w-[80vw]" :footer="false">
    <div class="flex h-[70vh] min-h-0 gap-2">
      <ul class="w-56 shrink-0 overflow-y-auto rounded border border-gray-200">
        <li
          v-for="(item, index) in files"
          :key="`${item.fileUrl}-${index}`"
          class="cursor-pointer truncate px-2 py-2 text-sm hover:bg-gray-100"
          :class="{ 'bg-gray-100 font-medium': index === activeIndex }"
          :title="item.fileName"
          @click="handleSelect(index)"
        >
          {{ index + 1 }}. {{ item.fileName }}
        </li>
        <li
          v-if="files.length === 0"
          class="px-2 py-2 text-sm text-gray-400"
        >
          暂无附件
        </li>
      </ul>
      <div class="min-w-0 flex-1 overflow-hidden">
        <FileViewer
          v-if="previewFile"
          :key="`${previewFile.name}-${previewFile.size}`"
          :file="previewFile"
          :options="fileViewerOptions"
        />
        <div
          v-else-if="loading"
          class="flex h-full items-center justify-center text-gray-400"
        >
          文件加载中...
        </div>
        <div
          v-else
          class="flex h-full items-center justify-center text-gray-400"
        >
          请选择左侧附件进行预览
        </div>
      </div>
    </div>
  </Modal>
</template>
