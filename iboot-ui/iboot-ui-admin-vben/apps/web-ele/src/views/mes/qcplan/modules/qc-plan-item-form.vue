<script lang="ts" setup>
import type { MesQcItemApi } from '#/api/mes/qcitem';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createQcItem, getQcItem, updateQcItem } from '#/api/mes/qcitem';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useQcItemFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesQcItemApi.QcItem>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['检验项目'])
    : $t('ui.actionTitle.create', ['检验项目']);
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
  schema: useQcItemFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MesQcItemApi.QcItem;
    // 父键不在表单里，提交时从抽屉传入的上下文回填，否则后端 planId 非空校验直接拒
    data.planId = formData.value?.planId;
    try {
      await (formData.value?.id ? updateQcItem(data) : createQcItem(data));
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
    const data = modalApi.getData<MesQcItemApi.QcItem>();
    if (!data) {
      return;
    }
    // 父键留在上下文里（新增只带 planId，编辑只带 id），提交时回填，不进表单
    formData.value = data;
    if (!data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表 RespVO 是裁剪过的，编辑必须回查详情再回填
      const item = await getQcItem(data.id);
      formData.value = item;
      await formApi.setValues(item);
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
