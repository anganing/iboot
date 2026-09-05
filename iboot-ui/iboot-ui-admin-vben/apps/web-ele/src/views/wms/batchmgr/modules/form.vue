<script lang="ts" setup>
import type { WmsBatchApi } from '#/api/wms/batchmgr';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { getBatch, updateBatchAttr } from '#/api/wms/batchmgr';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useAttrFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<WmsBatchApi.Batch>();

/** 标题带上批次号，弹窗内只放六个可维护字段，身份字段靠标题给上下文 */
const getTitle = computed(() =>
  formData.value?.lotNo
    ? `维护批属性 - ${formData.value.lotNo}`
    : '维护批属性',
);

const formLayout = getMdmFormLayout(1);

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
  schema: useAttrFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 表单里只有六个批属性字段，getValues 拿到的就是 /update-attr 需要的全部入参
    const data = (await formApi.getValues()) as WmsBatchApi.BatchAttrUpdate;
    try {
      await updateBatchAttr(data);
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
    await formApi.resetForm();
    // 批属性只能改已存在的批次，没有 id 就是调用方传错了
    const data = modalApi.getData<WmsBatchApi.Batch>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表 RespVO 是裁剪过的，编辑前先取完整数据
      formData.value = await getBatch(data.id);
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
