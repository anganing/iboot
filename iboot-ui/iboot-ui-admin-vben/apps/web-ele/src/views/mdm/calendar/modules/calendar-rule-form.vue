<script lang="ts" setup>
import type { MdmCalendarApi } from '#/api/mdm/calendar';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createCalendarRule,
  getCalendarRule,
  updateCalendarRule,
} from '#/api/mdm/calendar';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useRuleFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MdmCalendarApi.CalendarRule>();
const getTitle = computed(() =>
  formData.value?.id ? '编辑常规规则' : '新增常规规则',
);
const formLayout = getMdmFormLayout(1);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 90,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useRuleFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.calendarId) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as MdmCalendarApi.CalendarRule;
    data.calendarId = formData.value.calendarId;
    try {
      await (formData.value.id
        ? updateCalendarRule(data)
        : createCalendarRule(data));
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
    const modalData = modalApi.getData<MdmCalendarApi.CalendarRule>();
    if (!modalData) {
      return;
    }
    formData.value = modalData;
    if (modalData.id) {
      modalApi.lock();
      try {
        formData.value = await getCalendarRule(modalData.id);
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
