<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createMaterialReqLine,
  getMaterialReqLine,
  updateMaterialReqLine,
} from '#/api/mes/materialreq';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useMaterialReqLineFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesMaterialReqApi.MaterialReqLine>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['申请明细'])
    : $t('ui.actionTitle.create', ['申请明细']);
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
  schema: useMaterialReqLineFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values =
      (await formApi.getValues()) as MesMaterialReqApi.MaterialReqLine;
    // 父键由父组件传入，不作为表单可见字段
    const data = { ...values, reqId: formData.value?.reqId };
    try {
      await (formData.value?.id
        ? updateMaterialReqLine(data)
        : createMaterialReqLine(data));
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
    const data = modalApi.getData<MesMaterialReqApi.MaterialReqLine>();
    formData.value = data;
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getMaterialReqLine(data.id);
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
