<script lang="ts" setup>
import type { MdmToolApi } from '#/api/mdm/tool';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createRouteOpTool,
  getRouteOpTool,
  updateRouteOpTool,
} from '#/api/mdm/tool';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useRouteOpToolFormSchema } from '../route-op-tool-data';

const emit = defineEmits(['success']);
const formData = ref<MdmToolApi.RouteOpTool>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑工序工装' : '新增工序工装',
);
const formLayout = getMdmFormLayout(2);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 80,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useRouteOpToolFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.routeOpId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmToolApi.RouteOpTool;
    data.routeOpId = formData.value.routeOpId;
    try {
      await (formData.value.id
        ? updateRouteOpTool(data)
        : createRouteOpTool(data));
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
    const modalData = modalApi.getData<MdmToolApi.RouteOpTool>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getRouteOpTool(modalData.id);
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
