<script lang="ts" setup>
import type { WmsArrivalApi } from '#/api/wms/arrival';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useArrivalLineFormSchema } from '../data';

/**
 * 到货明细行弹窗。
 *
 * 只负责收集一行明细并回传给单据表单，**不发任何请求**：明细的增删改端点已随写入路径收口撤销，
 * 明细只能随单头一次提交。
 */
const emit = defineEmits(['success']);
const formData = ref<WmsArrivalApi.ArrivalLine>();
/**
 * 关联选择器带回来的可读字段。
 *
 * 只用于明细表格显示：物料编码、名称、规格与单位编码这四个快照由领域 Service 从 MaterialApi、
 * UomApi 批量取后落库，前端传什么都会被覆盖，但界面上不能只显示裸 ID。
 */
const snapshot = ref<Record<string, any>>({});

const getTitle = computed(() =>
  formData.value
    ? $t('ui.actionTitle.edit', ['到货明细'])
    : $t('ui.actionTitle.create', ['到货明细']),
);

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
  schema: useArrivalLineFormSchema((field, row) => {
    switch (field) {
      case 'matId': {
        snapshot.value.matCode = row?.code;
        snapshot.value.matName = row?.name;
        snapshot.value.matSpec = row?.spec;
        break;
      }
      case 'uomId': {
        snapshot.value.uomCode = row?.code;
        break;
      }
      // 其余字段没有快照要回填
      default: {
        break;
      }
    }
  }),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const values = (await formApi.getValues()) as WmsArrivalApi.ArrivalLine;
    emit('success', {
      ...formData.value,
      ...values,
      ...snapshot.value,
    });
    await modalApi.close();
  },  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      snapshot.value = {};
      return;
    }
    await formApi.resetForm();
    snapshot.value = {};
    const data = modalApi.getData<WmsArrivalApi.ArrivalLine>();
    formData.value = data ?? undefined;
    if (data) {
      await formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle" :z-index="1200">
    <Form class="mx-4" />
  </Modal>
</template>
