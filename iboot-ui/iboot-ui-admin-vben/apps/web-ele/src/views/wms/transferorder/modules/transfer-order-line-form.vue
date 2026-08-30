<script lang="ts" setup>
  import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getTransferOrderLine, createTransferOrderLine, updateTransferOrderLine } from '#/api/wms/transferorder';
  import { $t } from '#/locales';

  import { useTransferOrderLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsTransferOrderApi.TransferOrderLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['WMS-库存转移调拨明细'])
        : $t('ui.actionTitle.create', ['WMS-库存转移调拨明细']);
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
  <Modal :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>