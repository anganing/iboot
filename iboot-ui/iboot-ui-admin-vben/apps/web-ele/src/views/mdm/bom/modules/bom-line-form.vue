<script lang="ts" setup>
import type { MdmBomApi } from '#/api/mdm/bom';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { getBomLine, createBomLine, updateBomLine } from '#/api/mdm/bom';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useBomLineFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MdmBomApi.BomLine>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['物料清单明细'])
    : $t('ui.actionTitle.create', ['物料清单明细']);
});

const formLayout = getMdmFormLayout(1);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 80,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useBomLineFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }

    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as MdmBomApi.BomLine;
    data.bomId = formData.value?.bomId;
    try {
      await (formData.value?.id ? updateBomLine(data) : createBomLine(data));
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

    // 加载数据
    let data = modalApi.getData<MdmBomApi.BomLine>();
    if (!data) {
      return;
    }
    if (data.id) {
      modalApi.lock();
      try {
        data = await getBomLine(data.id);
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
