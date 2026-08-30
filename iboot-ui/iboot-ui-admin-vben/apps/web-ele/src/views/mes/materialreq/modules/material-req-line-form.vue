<script lang="ts" setup>
  import type { MesMaterialReqApi } from '#/api/mes/materialreq';

  import { computed, ref } from 'vue';

  import { useVbenModal } from '@vben/common-ui';

  import { ElMessage } from 'element-plus';

  import { useVbenForm } from '#/adapter/form';
  import { getMaterialReqLine, createMaterialReqLine, updateMaterialReqLine } from '#/api/mes/materialreq';
  import { $t } from '#/locales';

  import { useMaterialReqLineFormSchema } from '../data';

  const emit = defineEmits(['success']);
  const formData = ref<MesMaterialReqApi.MaterialReqLine>();
  const getTitle = computed(() => {
    return formData.value?.id
        ? $t('ui.actionTitle.edit', ['MES-领退补料申请明细'])
        : $t('ui.actionTitle.create', ['MES-领退补料申请明细']);
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
    schema: useMaterialReqLineFormSchema(),
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
      const data = (await formApi.getValues()) as MesMaterialReqApi.MaterialReqLine;
      data.reqId = formData.value?.reqId;
      try {
        await (formData.value?.id ? updateMaterialReqLine(data) : createMaterialReqLine(data));
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
      let data = modalApi.getData<MesMaterialReqApi.MaterialReqLine>();
      if (!data) {
        return;
      }
      if (data.id) {
        modalApi.lock();
        try {
          data = await getMaterialReqLine(data.id);
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