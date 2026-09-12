<script lang="ts" setup>
import type { WmsTransferOrderApi } from '#/api/wms/transferorder';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useTransferOrderLineFormSchema } from '../data';

/**
 * 调拨明细行弹窗。
 *
 * 只负责收集一行明细并回传给单据表单，**不发任何请求**：明细的增删改端点已随写入路径收口撤销，
 * 明细只能随单头一次提交。
 */
const emit = defineEmits(['success']);
const formData = ref<WmsTransferOrderApi.TransferOrderLine>();
/**
 * 关联选择器带回来的可读字段。
 *
 * 转移单和入库单不一样：物料编码、物料名称、批次号、基准单位编码在 `wms_transfer_order_line` 上
 * 都是 NOT NULL 且无默认值，领域侧又不回填物料快照，所以这些快照不是"为了好看"，是插库的必要条件。
 * 库位编码与容器编号只用于明细表格显示。
 */
const snapshot = ref<Record<string, any>>({});

const getTitle = computed(() =>
  formData.value
    ? $t('ui.actionTitle.edit', ['调拨明细'])
    : $t('ui.actionTitle.create', ['调拨明细']),
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
  schema: useTransferOrderLineFormSchema((field, row) => {
    switch (field) {
      case 'batchId': {
        snapshot.value.lotNo = row?.lotNo;
        break;
      }
      case 'containerId': {
        snapshot.value.containerNo = row?.containerNo;
        break;
      }
      case 'fromLocId': {
        snapshot.value.fromLocCode = row?.code;
        break;
      }
      case 'matId': {
        snapshot.value.matCode = row?.code;
        snapshot.value.matName = row?.name;
        break;
      }
      case 'toLocId': {
        snapshot.value.toLocCode = row?.code;
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
    const values =
      (await formApi.getValues()) as WmsTransferOrderApi.TransferOrderLine;
    emit('success', {
      ...formData.value,
      ...values,
      ...snapshot.value,
    });
    await modalApi.close();
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      snapshot.value = {};
      return;
    }
    await formApi.resetForm();
    snapshot.value = {};
    const data = modalApi.getData<WmsTransferOrderApi.TransferOrderLine>();
    formData.value = data ?? undefined;
    if (data) {
      await formApi.setValues(data);
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
