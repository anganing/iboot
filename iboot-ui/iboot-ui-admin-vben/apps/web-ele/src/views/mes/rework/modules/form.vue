<script lang="ts" setup>
import type { MesReworkApi } from '#/api/mes/rework';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createRework, getRework, updateRework } from '#/api/mes/rework';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesReworkApi.Rework>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['返工任务'])
    : $t('ui.actionTitle.create', ['返工任务']);
});

const formLayout = getMdmFormLayout(2);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 110,
  },
  wrapperClass: formLayout.wrapperClass,
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
    const data = (await formApi.getValues()) as MesReworkApi.Rework;
    try {
      await (formData.value?.id ? updateRework(data) : createRework(data));
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
    await formApi.resetForm();
    const data = modalApi.getData<MesReworkApi.Rework>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表 RespVO 是裁剪过的，编辑必须回查详情再回填
      formData.value = await getRework(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
