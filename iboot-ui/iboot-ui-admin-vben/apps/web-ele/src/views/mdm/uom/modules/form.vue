<script lang="ts" setup>
import type { MdmUomApi } from '#/api/mdm/uom';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createUom, getUom, updateUom } from '#/api/mdm/uom';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MdmUomApi.Uom>();
const title = computed(() =>
  formData.value?.id ? '编辑计量单位' : '新建计量单位',
);
const formLayout = getMdmFormLayout(1, 'w-2/5');
const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 100,
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
      const data = (await formApi.getValues()) as MdmUomApi.Uom;
      await (formData.value?.id ? updateUom(data) : createUom(data));
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
    const data = modalApi.getData<MdmUomApi.Uom>();
    if (!data?.id) {
      await formApi.resetForm();
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getUom(data.id);
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
