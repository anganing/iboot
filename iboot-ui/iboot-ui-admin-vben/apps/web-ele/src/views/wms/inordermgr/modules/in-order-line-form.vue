<script lang="ts" setup>
  import type { WmsInOrderApi } from '#/api/wms/inordermgr';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getInOrderLine, createInOrderLine, updateInOrderLine } from '#/api/wms/inordermgr';
  import { $t } from '#/locales';
  import { getMdmFormLayout } from '#/views/mdm/form-layout';

  import { useInOrderLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsInOrderApi.InOrderLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['入库明细'])
        : $t('ui.actionTitle.create', ['入库明细']);
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
    schema: useInOrderLineFormSchema(),
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
      const data = (await formApi.getValues()) as WmsInOrderApi.InOrderLine;
      data.orderId = formData.value?.orderId;
      try {
        await (formData.value?.id ? updateInOrderLine(data) : createInOrderLine(data));
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
      let data = modalApi.getData<WmsInOrderApi.InOrderLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getInOrderLine(data.id);
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