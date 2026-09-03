<script lang="ts" setup>
import type { CSSProperties } from 'vue';

import type { CropperAvatarProps } from './typing';

import { computed, ref, unref, watch, watchEffect } from 'vue';

import { useVbenModal, VbenAvatar } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { ElButton, ElMessage } from 'element-plus';

import cropperModal from './cropper-modal.vue';

defineOptions({ name: 'CropperAvatar' });

const props = withDefaults(defineProps<CropperAvatarProps>(), {
  width: 200,
  value: '',
  showBtn: true,
  btnProps: () => ({}) as any,
  btnText: '',
  uploadApi: () => Promise.resolve(),
  size: 5,
});

const emit = defineEmits(['update:value', 'change']);

const sourceValue = ref(props.value || '');
const [CropperModal, modalApi] = useVbenModal({
  connectedComponent: cropperModal,
});

const getWidth = computed(() => `${`${props.width}`.replace(/px/, '')}px`);

const getIconWidth = computed(
  () => `${Number.parseInt(`${props.width}`.replace(/px/, '')) / 2}px`,
);

const getStyle = computed((): CSSProperties => ({ width: unref(getWidth) }));

const getImageWrapperStyle = computed(
  (): CSSProperties => ({ height: unref(getWidth), width: unref(getWidth) }),
);

watchEffect(() => {
  sourceValue.value = props.value || '';
});

watch(
  () => sourceValue.value,
  (v: string) => {
    emit('update:value', v);
  },
);

function handleUploadSuccess({ data, source }: any) {
  sourceValue.value = source;
  emit('change', { data, source });
  ElMessage.success($t('ui.cropper.uploadSuccess'));
}

const closeModal = () => modalApi.close();
const openModal = () => modalApi.open();

defineExpose({
  closeModal,
  openModal,
});
</script>

<template>
  <!-- 头像容器 -->
  <div class="inline-block text-center" :style="getStyle">
    <!-- 图片包装器 -->
    <div
      class="group border-border bg-background relative cursor-pointer overflow-hidden rounded-full border"
      :style="getImageWrapperStyle"
      @click="openModal"
    >
      <!-- 遮罩层：VbenAvatar 根节点是 relative 定位，遮罩需要显式抬高层级才能盖在头像之上 -->
      <div
        class="duration-400 absolute inset-0 z-10 flex cursor-pointer items-center justify-center rounded-full bg-black bg-opacity-40 opacity-0 transition-opacity group-hover:opacity-100"
        :style="getImageWrapperStyle"
      >
        <IconifyIcon
          icon="lucide:cloud-upload"
          class="m-auto text-gray-400"
          :style="{
            ...getImageWrapperStyle,
            width: getIconWidth,
            height: getIconWidth,
            lineHeight: getIconWidth,
          }"
        />
      </div>
      <!-- 头像与全站同构：有图显示图片，无图或加载失败回落到文字头像 -->
      <VbenAvatar :alt="name" :src="sourceValue" class="size-full" />
    </div>
    <!-- 上传按钮 -->
    <ElButton
      v-if="showBtn"
      class="mx-auto mt-2"
      @click="openModal"
      v-bind="btnProps"
    >
      {{ btnText ? btnText : $t('ui.cropper.selectImage') }}
    </ElButton>

    <CropperModal
      :size="size"
      :src="sourceValue"
      :upload-api="uploadApi"
      @upload-success="handleUploadSuccess"
    />
  </div>
</template>
