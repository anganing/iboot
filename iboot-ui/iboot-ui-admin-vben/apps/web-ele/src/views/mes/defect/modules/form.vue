<script lang="ts" setup>
import type { MesDefectApi } from '#/api/mes/defect';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createDefect, getDefect, updateDefect } from '#/api/mes/defect';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<MesDefectApi.Defect>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['缺陷记录'])
    : $t('ui.actionTitle.create', ['缺陷记录']);
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
    const data = (await formApi.getValues()) as MesDefectApi.Defect;
    // 只回传后端 ReqVO 认识的三个字段，剥掉回显带来的 id/seq/fileType
    data.files = (data.files ?? []).map((file) => ({
      fileName: file.fileName,
      fileUrl: file.fileUrl,
      fileId: file.fileId,
    }));
    try {
      await (formData.value?.id ? updateDefect(data) : createDefect(data));
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
    const data = modalApi.getData<MesDefectApi.Defect>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表不返回附件明细，编辑必须回查详情才能拿到附件
      formData.value = await getDefect(data.id);
      await formApi.setValues(formData.value);
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
