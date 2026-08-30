<script lang="ts" setup>
  import type { WmsCountOrderApi } from '#/api/wms/countorder';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getCountOrderLine, createCountOrderLine, updateCountOrderLine } from '#/api/wms/countorder';
  import { $t } from '#/locales';

  import { useCountOrderLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsCountOrderApi.CountOrderLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['WMS-盘点明细'])
        : $t('ui.actionTitle.create', ['WMS-盘点明细']);
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
    schema: useCountOrderLineFormSchema(),
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
      const data = (await formApi.getValues()) as WmsCountOrderApi.CountOrderLine;
      data.orderId = formData.value?.orderId;
      try {
        await (formData.value?.id ? updateCountOrderLine(data) : createCountOrderLine(data));
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
      let data = modalApi.getData<WmsCountOrderApi.CountOrderLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getCountOrderLine(data.id);
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