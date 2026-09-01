<script lang="ts" setup>
import type { ApsShortageApi } from '#/api/aps/shortage';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { getShortage, handleShortage } from '#/api/aps/shortage';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useHandleFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<ApsShortageApi.Shortage>();
const formLayout = getMdmFormLayout(2);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 110,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useHandleFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    const values = (await formApi.getValues()) as ApsShortageApi.Shortage;
    try {
      await handleShortage({
        id: values.id!,
        expectedSupplyDate: values.expectedSupplyDate,
        remark: values.remark,
      });
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
    const data = modalApi.getData<ApsShortageApi.Shortage>();
    if (!data?.id) return;
    modalApi.lock();
    try {
      formData.value = await getShortage(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" title="处理缺料">
    <Form class="mx-4" />
  </Modal>
</template>
