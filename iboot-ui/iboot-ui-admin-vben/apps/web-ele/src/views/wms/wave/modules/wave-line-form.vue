<script lang="ts" setup>
  import type { WmsWaveApi } from '#/api/wms/wave';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getWaveLine, createWaveLine, updateWaveLine } from '#/api/wms/wave';
  import { $t } from '#/locales';
  import { getMdmFormLayout } from '#/views/mdm/form-layout';

  import { useWaveLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<WmsWaveApi.WaveLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['波次明细'])
        : $t('ui.actionTitle.create', ['波次明细']);
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
    schema: useWaveLineFormSchema(),
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
      const data = (await formApi.getValues()) as WmsWaveApi.WaveLine;
      data.waveId = formData.value?.waveId;
      try {
        await (formData.value?.id ? updateWaveLine(data) : createWaveLine(data));
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
      let data = modalApi.getData<WmsWaveApi.WaveLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getWaveLine(data.id);
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