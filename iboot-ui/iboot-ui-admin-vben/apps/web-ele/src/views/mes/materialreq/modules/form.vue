<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createMaterialReq } from '#/api/mes/materialreq';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useCreateFormSchema } from '../data';

const emit = defineEmits(['success']);
const getTitle = computed(() => {
  return $t('ui.actionTitle.create', ['领退补料申请']);
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
  schema: useCreateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data =
      (await formApi.getValues()) as MesMaterialReqApi.MaterialReqCreateReq;
    try {
      await createMaterialReq(data);
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
