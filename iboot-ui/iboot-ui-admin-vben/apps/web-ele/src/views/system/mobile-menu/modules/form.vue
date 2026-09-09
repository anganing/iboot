<script lang="ts" setup>
import type { SystemMobileMenuApi } from '#/api/system/mobile-menu';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createMobileMenu,
  getMobileMenu,
  updateMobileMenu,
} from '#/api/system/mobile-menu';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<SystemMobileMenuApi.MobileMenu>();
const getTitle = computed(() =>
  formData.value?.id
    ? $t('ui.actionTitle.edit', ['移动端菜单'])
    : $t('ui.actionTitle.create', ['移动端菜单']),
);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as SystemMobileMenuApi.MobileMenu;
    try {
      await (formData.value?.id
        ? updateMobileMenu(data)
        : createMobileMenu(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<SystemMobileMenuApi.MobileMenu>();
    if (!data || !data.id) {
      // 设置上级
      await formApi.setValues(data);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getMobileMenu(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
