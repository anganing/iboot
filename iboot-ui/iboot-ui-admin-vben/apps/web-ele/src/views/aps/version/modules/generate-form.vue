<script lang="ts" setup>
import type { ApsVersionApi } from '#/api/aps/version';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { generateVersion } from '#/api/aps/version';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useGenerateFormSchema } from '../data';

/** 提交成功后把新版本编号交给列表页打开进度抽屉 */
const emit = defineEmits<{ success: [versionId: number] }>();

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
  schema: useGenerateFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const data = (await formApi.getValues()) as ApsVersionApi.GenerateReq;
    try {
      // 接口是异步的：立刻返回版本编号，求解在后台跑，进度靠 WebSocket 推
      const versionId = await generateVersion(data);
      await modalApi.close();
      ElMessage.success('已提交求解，进度将实时推送');
      emit('success', versionId);
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
    // 从列表行带入场景，省掉一次选择
    const data = modalApi.getData<{ scenarioId?: number }>();
    if (data?.scenarioId) {
      await formApi.setValues({ scenarioId: data.scenarioId });
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" title="生成排程版本">
    <Form class="mx-4" />
  </Modal>
</template>
