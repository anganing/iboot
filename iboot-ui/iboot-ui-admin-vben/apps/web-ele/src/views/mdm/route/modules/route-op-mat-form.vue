<script lang="ts" setup>
import type { MdmRouteOpMatApi } from '#/api/mdm/routeopmat';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createRouteOpMat,
  getRouteOpMat,
  updateRouteOpMat,
} from '#/api/mdm/routeopmat';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useRouteOpMatFormSchema } from '../route-op-mat-data';

const emit = defineEmits(['success']);
const formData = ref<MdmRouteOpMatApi.RouteOpMat>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑工序用料' : '新增工序用料',
);
const formLayout = getMdmFormLayout(1);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 80,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useRouteOpMatFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.routeOpId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmRouteOpMatApi.RouteOpMat;
    data.routeOpId = formData.value.routeOpId;
    try {
      await (formData.value.id
        ? updateRouteOpMat(data)
        : createRouteOpMat(data));
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
    const modalData = modalApi.getData<MdmRouteOpMatApi.RouteOpMat>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getRouteOpMat(modalData.id);
      } finally {
        modalApi.unlock();
      }
    }
    await formApi.setValues(formData.value);
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
