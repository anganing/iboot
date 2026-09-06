<script lang="ts" setup>
import type { MesProdReportApi } from '#/api/mes/prodreport';

import { computed } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { submitProdReport } from '#/api/mes/prodreport';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useSubmitFormSchema } from '../data';

const emit = defineEmits(['success']);
const getTitle = computed(() => {
  return $t('ui.actionTitle.create', ['生产报工']);
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
  schema: useSubmitFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data =
      (await formApi.getValues()) as MesProdReportApi.ProdReportSubmitReq;
    try {
      await submitProdReport(data);
      await modalApi.close();
      emit('success');
      ElMessage.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
