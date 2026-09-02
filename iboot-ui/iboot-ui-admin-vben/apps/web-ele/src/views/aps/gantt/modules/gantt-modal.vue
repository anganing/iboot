<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import GanttView from './gantt-view.vue';

/**
 * 排程甘特图弹窗。
 *
 * <p>默认全屏打开：甘特图要同时看资源行与时间轴，常规弹窗宽度里横滚两屏就失去了纵览意义。
 * 版本由排程版本列表选定后 setData 带进来，弹窗内不再提供版本选择器。
 */
const versionId = ref<number>();
const title = ref('排程甘特图');

const [Modal, modalApi] = useVbenModal({
  contentClass: 'overflow-hidden p-3',
  // 关闭即销毁：GanttView 卸载时会 release 掉 Gantt 持有的 canvas 与事件监听
  destroyOnClose: true,
  footer: false,
  fullscreen: true,
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    const data = modalApi.getData<{ title?: string; versionId?: number }>();
    versionId.value = data?.versionId;
    title.value = data?.title ? `排程甘特图 - ${data.title}` : '排程甘特图';
  },
});
</script>

<template>
  <Modal :title="title">
    <GanttView :version-id="versionId" />
  </Modal>
</template>
