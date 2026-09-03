<script setup lang="ts">
import { computed } from 'vue';

interface Props {
  /** 富文本内容，HTML 字符串 */
  content?: string;
  /** 内容为空时的占位文案 */
  emptyText?: string;
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  emptyText: '暂无内容',
});

const hasContent = computed(() => {
  const content = props.content?.trim();
  // 富文本编辑器留下的空段落不算内容
  return !!content && content !== '<p></p>' && content !== '<p><br></p>';
});
</script>

<template>
  <!-- 用 v-dompurify-html 而非 v-html：内容由模板与业务参数拼接而成，清洗放在渲染侧 -->
  <div
    v-if="hasContent"
    v-dompurify-html="content"
    class="prose prose-sm dark:prose-invert max-w-none break-words"
  ></div>
  <div v-else class="text-sm text-muted-foreground">{{ emptyText }}</div>
</template>
