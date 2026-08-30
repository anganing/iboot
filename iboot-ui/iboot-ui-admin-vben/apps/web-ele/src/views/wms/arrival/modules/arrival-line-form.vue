<script lang="ts" setup>
  import type { WmsArrivalApi } from '#/api/wms/arrival';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getArrivalLine, createArrivalLine, updateArrivalLine } from '#/api/wms/arrival';
  import { $t } from '#/locales';

  import { useArrivalLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsArrivalApi.ArrivalLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['WMS-到货登记明细'])
        : $t('ui.actionTitle.create', ['WMS-到货登记明细']);
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
    schema: useArrivalLineFormSchema(),
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
      const data = (await formApi.getValues()) as WmsArrivalApi.ArrivalLine;
      data.arrivalId = formData.value?.arrivalId;
      try {
        await (formData.value?.id ? updateArrivalLine(data) : createArrivalLine(data));
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
      let data = modalApi.getData<WmsArrivalApi.ArrivalLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getArrivalLine(data.id);
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