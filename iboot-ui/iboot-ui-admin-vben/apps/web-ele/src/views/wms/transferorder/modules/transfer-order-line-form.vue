<script lang="ts" setup>
  import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { createTransferOrderLine, getTransferOrderLine, updateTransferOrderLine } from '#/api/wms/transferorder';
  import { $t } from '#/locales';
  import { getMdmFormLayout } from '#/views/mdm/form-layout';

  import { useTransferOrderLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsTransferOrderApi.TransferOrderLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['调拨明细'])
        : $t('ui.actionTitle.create', ['调拨明细']);
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
    schema: useTransferOrderLineFormSchema(),
    showDefaultActions: false
  });

  const [Modal, modalApi] = useVbenModal({
    async onConfirm() {
      const { valid } = await formApi.validate();
      if (!valid) {
        return;
      }

      modalApi.lock();
      // 提交表单
      const data = (await formApi.getValues()) as WmsTransferOrderApi.TransferOrderLine;
      data.orderId = formData.value?.orderId;
      try {
        await (formData.value?.id ? updateTransferOrderLine(data) : createTransferOrderLine(data));
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
      let data = modalApi.getData<WmsTransferOrderApi.TransferOrderLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getTransferOrderLine(data.id);
        } finally {
          modalApi.unlock();
        }
      }
      // 设置到 values
      formData.value = data;
      await formApi.setValues(formData.value);
    },
  });
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>