<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import GanttView from './gantt-view.vue';

/**
 * 排程甘特图弹窗。
 *
 * <p>默认全屏打开：甘特图要同时看资源行与时间轴，常规弹窗宽度里横滚两屏就失去了纵览意义。
 * 版本由外层列表选定后 setData 带进来，弹窗内不再提供版本选择器。
 *
 * <p>{@code readonly} 同样由外层决定：排程版本列表是计划员评审求解结果的地方，可以点开调整；
 * 排程结果页面向车间，只看不改。
 */
const versionId = ref<number>();
const readonly = ref(false);
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
    const data = modalApi.getData<{
      readonly?: boolean;
      title?: string;
      versionId?: number;
    }>();
    versionId.value = data?.versionId;
    readonly.value = data?.readonly ?? false;
    title.value = data?.title ? `排程甘特图 - ${data.title}` : '排程甘特图';
  },
});
</script>

<template>
  <Modal :title="title">
    <GanttView :readonly="readonly" :version-id="versionId" />
  </Modal>
</template>
