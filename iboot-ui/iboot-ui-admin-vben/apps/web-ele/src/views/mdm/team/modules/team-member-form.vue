<script lang="ts" setup>
import type { MdmTeamApi } from '#/api/mdm/team';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createTeamMember,
  getTeamMember,
  updateTeamMember,
} from '#/api/mdm/team';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useTeamMemberFormSchema } from '../team-member-data';

const emit = defineEmits(['success']);
const formData = ref<MdmTeamApi.TeamMember>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑班组成员' : '新增班组成员',
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
  schema: useTeamMemberFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.teamId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmTeamApi.TeamMember;
    data.teamId = formData.value.teamId;
    try {
      await (formData.value.id
        ? updateTeamMember(data)
        : createTeamMember(data));
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
    const modalData = modalApi.getData<MdmTeamApi.TeamMember>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getTeamMember(modalData.id);
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
