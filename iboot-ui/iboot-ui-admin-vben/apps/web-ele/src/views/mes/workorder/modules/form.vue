<script lang="ts" setup>
import type { MesWorkOrderApi } from '#/api/mes/workorder';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createWorkOrder,
  getWorkOrder,
  updateWorkOrder,
} from '#/api/mes/workorder';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesWorkOrderApi.WorkOrder>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['作业工单'])
    : $t('ui.actionTitle.create', ['作业工单']);
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
    const data = (await formApi.getValues()) as MesWorkOrderApi.WorkOrder;
    try {
      await (formData.value?.id ? updateWorkOrder(data) : createWorkOrder(data));
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
    const data = modalApi.getData<MesWorkOrderApi.WorkOrder>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表 RespVO 是裁剪过的，编辑必须回查详情再回填
      formData.value = await getWorkOrder(data.id);
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
