<script lang="ts" setup>
import type { UploadRequestOptions } from 'element-plus';

import type { MesDefectApi } from '#/api/mes/defect';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { ElButton, ElMessage, ElUpload } from 'element-plus';

import { useUpload } from '#/components/upload/use-upload';

defineOptions({ name: 'DefectFileField' });

/**
 * 缺陷记录附件字段：支持图片/视频/PDF/图纸/Office 多文件上传。
 *
 * 文件名直接取自 File.name，不从 URL 反解 —— URL 末段格式依赖存储器实现，反解出来的名字不可靠。
 * seq 与 fileType 由后端按下标与扩展名判定，前端不传。
 */
const props = withDefaults(
  defineProps<{
    disabled?: boolean;
    maxNumber?: number;
    maxSize?: number;
    modelValue?: MesDefectApi.DefectFile[];
  }>(),
  {
    disabled: false,
    maxNumber: 10,
    maxSize: 50,
    modelValue: () => [],
  },
);

const emit = defineEmits(['change', 'update:modelValue']);

/** 与后端 MesFileTypeEnum 的扩展名白名单保持一致 */
const ACCEPT_EXTENSIONS = [
  'jpg',
  'jpeg',
  'png',
  'gif',
  'bmp',
  'webp',
  'svg',
  'mp4',
  'avi',
  'mov',
  'wmv',
  'mkv',
  'flv',
  'webm',
  'pdf',
  'dwg',
  'dxf',
  'step',
  'stp',
  'igs',
  'iges',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'txt',
  'csv',
];

const accept = ACCEPT_EXTENSIONS.map((item) => `.${item}`).join(',');

const fileList = ref<MesDefectApi.DefectFile[]>([]);
const uploading = ref(0);

/** 判断两个附件列表是否内容相同（按文件地址与文件名顺序比对） */
function isSameList(
  left?: MesDefectApi.DefectFile[],
  right?: MesDefectApi.DefectFile[],
) {
  const a = left ?? [];
  const b = right ?? [];
  return (
    a.length === b.length &&
    a.every(
      (item, index) =>
        item.fileUrl === b[index]?.fileUrl &&
        item.fileName === b[index]?.fileName,
    )
  );
}

watch(
  () => props.modelValue,
  (value) => {
    // 外部值与内部列表内容一致时跳过，避免自身 emit 触发回写抖动
    if (isSameList(value, fileList.value)) {
      return;
    }
    fileList.value = (value ?? []).map((item) => ({ ...item }));
  },
  { immediate: true, deep: true },
);

const canUpload = computed(
  () => !props.disabled && fileList.value.length < props.maxNumber,
);

/** 同步内部列表到表单值 */
function syncValue() {
  const value = fileList.value.map((item) => ({ ...item }));
  emit('update:modelValue', value);
  emit('change', value);
}

/** 上传前校验数量、类型与大小 */
function beforeUpload(file: File) {
  if (fileList.value.length + uploading.value >= props.maxNumber) {
    ElMessage.error(`附件数量不能超过 ${props.maxNumber} 个`);
    return false;
  }
  const extension = file.name.split('.').pop()?.toLowerCase() ?? '';
  if (!ACCEPT_EXTENSIONS.includes(extension)) {
    ElMessage.error('不支持的文件格式，请上传图片/视频/PDF/图纸/Office 文件');
    return false;
  }
  if (file.size / 1024 / 1024 > props.maxSize) {
    ElMessage.error(`单个附件大小不能超过 ${props.maxSize}MB`);
    return false;
  }
  uploading.value += 1;
  return true;
}

/** 自定义上传：成功后按原始文件名入列 */
async function customRequest(options: UploadRequestOptions) {
  const file = options.file as File;
  try {
    const res: any = await useUpload().httpRequest(file);
    const fileUrl = res?.url ?? res?.data ?? res;
    fileList.value.push({ fileName: file.name, fileUrl });
    syncValue();
    options.onSuccess?.(res);
    ElMessage.success(`${file.name} 上传成功`);
  } catch (error: any) {
    options.onError?.(error);
    ElMessage.error(`${file.name} 上传失败`);
  } finally {
    uploading.value = Math.max(0, uploading.value - 1);
  }
}

/** 删除指定附件 */
function handleRemove(index: number) {
  fileList.value.splice(index, 1);
  syncValue();
}

/** 上移/下移调整附件展示顺序，后端按数组下标生成 seq */
function handleMove(index: number, offset: number) {
  const target = index + offset;
  if (target < 0 || target >= fileList.value.length) {
    return;
  }
  const [item] = fileList.value.splice(index, 1);
  fileList.value.splice(target, 0, item!);
  syncValue();
}
</script>

<template>
  <div class="w-full">
    <ElUpload
      :accept="accept"
      :before-upload="beforeUpload"
      :disabled="!canUpload"
      :http-request="customRequest"
      :show-file-list="false"
      multiple
    >
      <ElButton :disabled="!canUpload" type="primary">
        <IconifyIcon class="mr-1" icon="lucide:cloud-upload" />
        上传附件
      </ElButton>
    </ElUpload>
    <div class="mt-1 text-xs text-gray-400">
      支持图片/视频/PDF/图纸/Office 格式，最多 {{ maxNumber }} 个，单个不超过
      {{ maxSize }}MB
    </div>
    <ul v-if="fileList.length > 0" class="mt-2 space-y-1">
      <li
        v-for="(item, index) in fileList"
        :key="`${item.fileUrl}-${index}`"
        class="flex items-center rounded border border-gray-200 px-2 py-1 text-sm"
      >
        <span class="mr-2 w-5 text-center text-gray-400">{{ index + 1 }}</span>
        <span class="flex-1 truncate" :title="item.fileName">
          {{ item.fileName }}
        </span>
        <ElButton
          :disabled="disabled || index === 0"
          link
          size="small"
          @click="handleMove(index, -1)"
        >
          上移
        </ElButton>
        <ElButton
          :disabled="disabled || index === fileList.length - 1"
          link
          size="small"
          @click="handleMove(index, 1)"
        >
          下移
        </ElButton>
        <ElButton
          :disabled="disabled"
          link
          size="small"
          type="danger"
          @click="handleRemove(index)"
        >
          删除
        </ElButton>
      </li>
    </ul>
  </div>
</template>
