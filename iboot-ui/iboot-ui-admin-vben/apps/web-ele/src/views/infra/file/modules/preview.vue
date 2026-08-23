<script lang="ts" setup>
import type { FileViewerOptions } from '@file-viewer/vue3';
import type { InfraFileApi } from '#/api/infra/file';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import allRenderers from '@file-viewer/preset-all';
import { FileViewer } from '@file-viewer/vue3';
import '@file-viewer/vue3/dist/file-viewer3.css';

import { requestClient } from '#/api/request';

const previewFile = ref<File>();
const previewTitle = ref('文件预览');
const loading = ref(false);

const viewerOptions: FileViewerOptions = {
  preset: allRenderers,
  rendererMode: 'replace',
  theme: 'light',
  styleIsolation: 'shadow',
  cad: {
    wasmPath: '/wasm/cad',
    workerUrl: '/wasm/cad/dwg-worker.js',
    dwfWasmUrl: '/wasm/cad/dwfv-render.wasm',
  },
  drawing: {
    viewerScriptUrl: '/vendor/drawio/viewer-static.min.js',
  },
  toolbar: {
    position: 'bottom-right',
    download: true,
    print: true,
    exportHtml: false,
  },
};

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  onOpenChange(isOpen: boolean) {
    if (isOpen) {
      const data = modalApi.getData<InfraFileApi.File>();
      if (data) {
        void loadPreview(data);
      }
    } else {
      previewFile.value = undefined;
      previewTitle.value = '文件预览';
    }
  },
});

/** 通过后端代理接口获取文件内容，避免浏览器直接跨域访问文件存储 */
function buildDownloadUrl(file: InfraFileApi.File) {
  if (file.configId && file.path) {
    const path = file.path.split('/').map(encodeURIComponent).join('/');
    return `/infra/file/${file.configId}/get/${path}`;
  }
  return file.url ?? '';
}

async function loadPreview(file: InfraFileApi.File) {
  const downloadUrl = buildDownloadUrl(file);
  if (!downloadUrl) {
    ElMessage.error('文件 URL 为空');
    return;
  }

  loading.value = true;
  previewTitle.value = file.name || '文件预览';
  try {
    const blob = await requestClient.download<Blob>(downloadUrl);
    previewFile.value = new File([blob], file.name || file.path, {
      type: file.type || blob.type,
    });
  } catch {
    ElMessage.error('文件加载失败，请检查文件是否存在或稍后重试');
    previewFile.value = undefined;
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Modal :title="previewTitle" class="w-[80vw]" :footer="false">
    <div class="h-[70vh] min-h-0 overflow-hidden">
      <FileViewer
        v-if="previewFile"
        :key="`${previewFile.name}-${previewFile.size}`"
        :file="previewFile"
        :options="viewerOptions"
      />
      <div
        v-else-if="loading"
        class="flex h-full items-center justify-center text-gray-400"
      >
        文件加载中...
      </div>
    </div>
  </Modal>
</template>
