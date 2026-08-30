<script lang="ts" setup>
import type { MesDispositionApi } from '#/api/mes/disposition';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createDisposition, getDisposition, updateDisposition } from '#/api/mes/disposition';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesDispositionApi.Disposition>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['MES-不合格品处置单'])
    : $t('ui.actionTitle.create', ['MES-不合格品处置单']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
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
    const data = (await formApi.getValues()) as MesDispositionApi.Disposition;
    try {
      await (formData.value?.id ? updateDisposition(data) : createDisposition(data));
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
    const data = modalApi.getData<MesDispositionApi.Disposition>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDisposition(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>