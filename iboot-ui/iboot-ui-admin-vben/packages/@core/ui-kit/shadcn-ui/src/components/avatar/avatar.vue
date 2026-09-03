<script setup lang="ts">
import type {
  AvatarFallbackProps,
  AvatarImageProps,
  AvatarRootProps,
} from 'reka-ui';

import type { CSSProperties } from 'vue';

import type { ClassType } from '@vben-core/typings';

import { computed, useTemplateRef } from 'vue';

import { getAvatarInitials } from '@vben-core/shared/utils';

import { useElementSize } from '@vueuse/core';

import { Avatar, AvatarFallback, AvatarImage } from '../../ui';

interface Props
  extends AvatarFallbackProps, AvatarRootProps, Omit<AvatarImageProps, 'src'> {
  alt?: string;
  class?: ClassType;
  dot?: boolean;
  dotClass?: ClassType;
  fit?: 'contain' | 'cover' | 'fill' | 'none' | 'scale-down';
  size?: number;
  /** 头像图片地址；为空时展示由 alt 生成的文字头像 */
  src?: string;
}

defineOptions({
  inheritAttrs: false,
});

const props = withDefaults(defineProps<Props>(), {
  alt: '',
  as: 'button',
  dot: false,
  dotClass: 'bg-green-500',
  fit: 'cover',
});

const imageStyle = computed<CSSProperties>(() => {
  const { fit } = props;
  if (fit) {
    return { objectFit: fit };
  }
  return {};
});

const rootRef = useTemplateRef<HTMLElement>('rootRef');
const { width: measuredWidth } = useElementSize(rootRef);

const text = computed(() => getAvatarInitials(props.alt));

/**
 * 文字回退的字号跟随头像盒子大小，避免小头像里两个汉字被挤爆。
 *
 * <p>宽度尚未测出（首帧为 0）时不写行内样式，交给默认的 text-xs 兜一帧。
 */
const textStyle = computed<CSSProperties>(() => {
  const boxSize =
    props.size !== undefined && props.size > 0
      ? props.size
      : measuredWidth.value;
  if (!boxSize) {
    return {};
  }
  // 两个汉字比两个拉丁字母宽得多，需要更小的系数
  const ratio =
    text.value.length === 2 && /[^\u0020-\u007E]/.test(text.value) ? 0.32 : 0.4;
  return { fontSize: `${Math.round(boxSize * ratio)}px`, lineHeight: 1 };
});

const rootStyle = computed(() => {
  return props.size !== undefined && props.size > 0
    ? {
        height: `${props.size}px`,
        width: `${props.size}px`,
      }
    : {};
});
</script>

<template>
  <div
    ref="rootRef"
    :class="props.class"
    :style="rootStyle"
    class="relative flex shrink-0 items-center"
  >
    <!-- key 随有无图片切换：重建 AvatarRoot 以重置图片加载状态，保证清空头像后能回落到文字 -->
    <Avatar
      :key="src ? 'image' : 'text'"
      :class="props.class"
      class="size-full"
    >
      <AvatarImage
        v-if="src"
        :alt="alt || 'avatar'"
        :src="src"
        :style="imageStyle"
      />
      <AvatarFallback
        :style="textStyle"
        class="bg-primary/10 text-primary dark:bg-primary/20 text-xs font-medium"
      >
        {{ text }}
      </AvatarFallback>
    </Avatar>
    <span
      v-if="dot"
      :class="dotClass"
      class="border-background absolute right-0 bottom-0 size-3 rounded-full border-2"
    >
    </span>
  </div>
</template>
