<script lang="ts" setup>
import type { ApsStrategyApi } from '#/api/aps/strategy';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElAlert, ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createStrategy,
  getStrategy,
  updateStrategy,
} from '#/api/aps/strategy';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { loadConstraintMetadata } from '../constraint-metadata';
import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<ApsStrategyApi.Strategy>();
/** 库里原配置无法识别时的提示语，来自后端解析失败的原因 */
const configInvalidReason = ref<string>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['排程策略'])
    : $t('ui.actionTitle.create', ['排程策略']);
});

// 权重编辑器要放 11 行、两栏，900px 会挤成一栏
const formLayout = getMdmFormLayout(2, 'w-[1080px]');

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
    // 提交表单
    const data = (await formApi.getValues()) as ApsStrategyApi.Strategy;
    try {
      await (formData.value?.id ? updateStrategy(data) : createStrategy(data));
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
      configInvalidReason.value = undefined;
      return;
    }
    await formApi.resetForm();
    // 加载数据
    const data = modalApi.getData<ApsStrategyApi.Strategy>();
    if (!data || !data.id) {
      // 新建：默认配置取自后端元数据，不在前端写第二份默认权重
      modalApi.lock();
      try {
        const metadata = await loadConstraintMetadata();
        await formApi.setValues({ config: metadata.defaultConfig });
      } finally {
        modalApi.unlock();
      }
      return;
    }
    modalApi.lock();
    try {
      // 列表行不含约束配置，编辑必须用 get 拿完整数据
      formData.value = await getStrategy(data.id);
      configInvalidReason.value = formData.value.configInvalid
        ? formData.value.configInvalidReason
        : undefined;
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <ElAlert
      v-if="configInvalidReason"
      :closable="false"
      class="mx-4 mb-2"
      show-icon
      type="warning"
    >
      <template #title>
        原有配置无法识别（{{ configInvalidReason }}），下方已按默认权重填充，保存后即修正。
      </template>
    </ElAlert>
    <Form class="mx-4" />
  </Modal>
</template>
