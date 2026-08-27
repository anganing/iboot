<script lang="ts" setup>
import type { MdmInvOrgApi } from '#/api/mdm/invorg';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createInvOrg, getInvOrg, updateInvOrg } from '#/api/mdm/invorg';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MdmInvOrgApi.InvOrg>();
const title = computed(() => `${formData.value?.id ? '编辑' : '新建'}库存组织`);

const formLayout = getMdmFormLayout(1, 'w-3/5');

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 90,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) return;
    modalApi.lock();
    try {
      const data = (await formApi.getValues()) as MdmInvOrgApi.InvOrg;
      await (formData.value?.id ? updateInvOrg(data) : createInvOrg(data));
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen) {
    if (!isOpen) {
      formData.value = undefined;
      await formApi.resetForm();
      return;
    }
    const data = modalApi.getData<MdmInvOrgApi.InvOrg>();
    if (!data?.id) {
      await formApi.resetForm();
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getInvOrg(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="title" :class="formLayout.modalClass">
    <Form class="mx-4" />
  </Modal>
</template>
