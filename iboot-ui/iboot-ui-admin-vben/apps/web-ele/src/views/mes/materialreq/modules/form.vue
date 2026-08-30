<script lang="ts" setup>
import type { MesMaterialReqApi } from '#/api/mes/materialreq';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage, ElTabs, ElTabPane } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createMaterialReq, getMaterialReq, updateMaterialReq } from '#/api/mes/materialreq';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesMaterialReqApi.MaterialReq>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['MES-领退补料申请'])
    : $t('ui.actionTitle.create', ['MES-领退补料申请']);
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
    const data = (await formApi.getValues()) as MesMaterialReqApi.MaterialReq;
    try {
      await (formData.value?.id ? updateMaterialReq(data) : createMaterialReq(data));
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
    const data = modalApi.getData<MesMaterialReqApi.MaterialReq>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getMaterialReq(data.id);
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