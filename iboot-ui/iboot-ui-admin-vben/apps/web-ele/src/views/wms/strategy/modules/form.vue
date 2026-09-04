<script lang="ts" setup>
import type { WmsStrategyApi } from '#/api/wms/strategy';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElAlert, ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { createStrategy, getStrategy, updateStrategy } from '#/api/wms/strategy';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<WmsStrategyApi.Strategy>();
/** 库里原配置无法识别时的提示语，来自后端解析失败的原因 */
const configInvalidReason = ref<string>();

/** 全局策略落库时的仓库编号，与后端 WmsConstants.GLOBAL_WH_ID 对齐 */
const GLOBAL_WH_ID = 0;
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['仓储策略'])
    : $t('ui.actionTitle.create', ['仓储策略']);
});

// 配置编辑器要并排放筛选条件与排序链两栏，900px 会挤成一栏
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
    const data = (await formApi.getValues()) as WmsStrategyApi.Strategy;
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
    const data = modalApi.getData<WmsStrategyApi.Strategy>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      // 列表行不带解析后的配置，编辑必须用 get 拿完整数据
      formData.value = await getStrategy(data.id);
      configInvalidReason.value = formData.value.configInvalid
        ? formData.value.configInvalidReason
        : undefined;
      // 设置到 values
      await formApi.setValues({
        ...formData.value,
        // 0 是"全局"的落库约定，在选择器里就该表现成没选；传 0 会让它去查一个不存在的仓库
        whId: formData.value.whId === GLOBAL_WH_ID ? undefined : formData.value.whId,
      });
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
        原有配置无法识别（{{ configInvalidReason }}），下方已按该类型的默认配置填充，保存后即修正。
      </template>
    </ElAlert>
    <Form class="mx-4" />
  </Modal>
</template>