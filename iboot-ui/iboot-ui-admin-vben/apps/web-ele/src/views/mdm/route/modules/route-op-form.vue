<script lang="ts" setup>
import type { MdmRouteApi } from '#/api/mdm/route';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createRouteOp, getRouteOp, updateRouteOp } from '#/api/mdm/route';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useRouteOpFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MdmRouteApi.RouteOp>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['工艺路线工序'])
    : $t('ui.actionTitle.create', ['工艺路线工序']);
});

const formLayout = getMdmFormLayout(2);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 150,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useRouteOpFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.routeId) {
      return;
    }

    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as MdmRouteApi.RouteOp;
    data.routeId = formData.value?.routeId;
    try {
      await (formData.value?.id ? updateRouteOp(data) : createRouteOp(data));
      // 关闭并提示
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
    // 加载数据
    let data = modalApi.getData<MdmRouteApi.RouteOp>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getRouteOp(data.id);
      } finally {
        modalApi.unlock();
      }
    }
    // 设置到 values
    formData.value = data;
    await formApi.setValues(formData.value);
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
