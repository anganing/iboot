<script lang="ts" setup>
import type { MdmSkillApi } from '#/api/mdm/skill';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createMemberSkill,
  getMemberSkill,
  updateMemberSkill,
} from '#/api/mdm/skill';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useMemberSkillFormSchema } from '../member-skill-data';

const emit = defineEmits(['success']);
const formData = ref<MdmSkillApi.MemberSkill>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑人员技能' : '新增人员技能',
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
  schema: useMemberSkillFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.skillId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmSkillApi.MemberSkill;
    data.skillId = formData.value.skillId;
    try {
      await (formData.value.id
        ? updateMemberSkill(data)
        : createMemberSkill(data));
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
    const modalData = modalApi.getData<MdmSkillApi.MemberSkill>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getMemberSkill(modalData.id);
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
