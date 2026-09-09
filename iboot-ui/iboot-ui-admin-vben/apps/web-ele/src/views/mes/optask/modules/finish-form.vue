<script lang="ts" setup>
import type { MesOpTaskApi } from '#/api/mes/optask';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { finishOpTask } from '#/api/mes/optask';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useOpTaskFinishFormSchema } from '../data';

const emit = defineEmits(['success']);
const task = ref<MesOpTaskApi.OpTask>();
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
  schema: useOpTaskFinishFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    try {
      const values = await formApi.getValues();
      await finishOpTask({
        id: task.value!.id!,
        qualifiedQty: values.qualifiedQty,
        scrapQty: values.scrapQty,
        reason: values.reason,
      });
      await modalApi.close();
      emit('success');
      ElMessage.success('已完工');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      task.value = undefined;
      return;
    }
    const data = modalApi.getData<MesOpTaskApi.OpTask>();
    task.value = data;
    await formApi.resetForm();
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" title="工序完工">
    <Form class="mx-4" />
  </Modal>
</template>
