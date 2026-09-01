<script lang="ts" setup>
import type { ApsTaskApi } from '#/api/aps/task';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { adjustTask, getTask } from '#/api/aps/task';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useAdjustFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<ApsTaskApi.Task>();

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
  schema: useAdjustFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values = (await formApi.getValues()) as ApsTaskApi.Task;
    try {
      // 只提交允许人工调整的字段，工单与工序快照不参与
      await adjustTask({
        id: values.id,
        resId: values.resId,
        startTime: values.startTime,
        endTime: values.endTime,
        sequenceNo: values.sequenceNo,
        remark: values.remark,
      });
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
    const data = modalApi.getData<ApsTaskApi.Task>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表行是裁剪过的，调整前先取完整任务
      formData.value = await getTask(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" title="调整排程任务">
    <Form class="mx-4" />
  </Modal>
</template>
