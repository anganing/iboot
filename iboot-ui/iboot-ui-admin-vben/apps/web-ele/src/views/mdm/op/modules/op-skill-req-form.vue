<script lang="ts" setup>
import type { MdmSkillApi } from '#/api/mdm/skill';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createOpSkillReq,
  getOpSkillReq,
  updateOpSkillReq,
} from '#/api/mdm/skill';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useOpSkillReqFormSchema } from '../op-skill-req-data';

const emit = defineEmits(['success']);
const formData = ref<MdmSkillApi.OpSkillReq>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑技能要求' : '新增技能要求',
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
  schema: useOpSkillReqFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.opId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmSkillApi.OpSkillReq;
    data.opId = formData.value.opId;
    try {
      await (formData.value.id
        ? updateOpSkillReq(data)
        : createOpSkillReq(data));
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
    const modalData = modalApi.getData<MdmSkillApi.OpSkillReq>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getOpSkillReq(modalData.id);
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
