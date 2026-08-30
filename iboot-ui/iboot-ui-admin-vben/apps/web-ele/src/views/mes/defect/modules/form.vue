<script lang="ts" setup>
import type { MesDefectApi } from '#/api/mes/defect';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createDefect, getDefect, updateDefect } from '#/api/mes/defect';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesDefectApi.Defect>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['MES-缺陷记录'])
    : $t('ui.actionTitle.create', ['MES-缺陷记录']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
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
    // 提交表单
    const data = (await formApi.getValues()) as MesDefectApi.Defect;
    try {
      await (formData.value?.id ? updateDefect(data) : createDefect(data));
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
    const data = modalApi.getData<MesDefectApi.Defect>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDefect(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>