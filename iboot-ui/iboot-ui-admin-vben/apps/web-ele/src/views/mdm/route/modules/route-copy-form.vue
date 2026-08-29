<script lang="ts" setup>
import type { MdmRouteApi } from '#/api/mdm/route';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { copyRoute } from '#/api/mdm/route';

defineOptions({ name: 'RouteCopyForm' });

const emit = defineEmits(['success']);

const source = ref<MdmRouteApi.Route>();
const copyForm = ref({
  version: '',
  effectiveDate: undefined as string | undefined,
});

const title = computed(() => {
  const route = source.value;
  return route
    ? `复制新版本 - ${[route.matCode, route.matName, route.version].filter(Boolean).join(' · ')}`
    : '复制新版本';
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    if (!source.value?.id) {
      return;
    }
    if (!copyForm.value.version.trim()) {
      ElMessage.warning('请输入新版本号');
      return;
    }
    modalApi.lock();
    try {
      await copyRoute({
        id: source.value.id,
        version: copyForm.value.version.trim(),
        effectiveDate: copyForm.value.effectiveDate,
      });
      await modalApi.close();
      emit('success');
      ElMessage.success('新版本已生成');
    } finally {
      modalApi.unlock();
    }
  },
  onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      source.value = undefined;
      copyForm.value = { version: '', effectiveDate: undefined };
      return;
    }
    source.value = modalApi.getData<MdmRouteApi.Route>();
  },
});
</script>

<template>
  <Modal class="w-[520px]" :title="title">
    <el-form class="mx-4" label-width="100px">
      <el-form-item label="新版本号" required>
        <el-input
          v-model="copyForm.version"
          :maxlength="32"
          placeholder="请输入新版本号，如 V2"
        />
      </el-form-item>
      <el-form-item label="生效日期">
        <el-date-picker
          v-model="copyForm.effectiveDate"
          class="!w-full"
          placeholder="为空则取当天"
          type="date"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>
    </el-form>
  </Modal>
</template>
