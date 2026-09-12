<script lang="ts" setup>
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { useVbenForm } from '#/adapter/form';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useInOrderLineFormSchema } from '../data';

/**
 * 入库明细行弹窗。
 *
 * 只负责收集一行明细并回传给单据表单，**不发任何请求**：明细的增删改端点已随写入路径收口撤销，
 * 明细只能随单头一次提交。
 */
const emit = defineEmits(['success']);
const formData = ref<WmsInOrderApi.InOrderLine>();
/** 关联选择器带回来的可读字段，明细表格靠它显示物料编码、库位编码而不是裸 ID */
const snapshot = ref<Record<string, any>>({});

const getTitle = computed(() =>
  formData.value
    ? $t('ui.actionTitle.edit', ['入库明细'])
    : $t('ui.actionTitle.create', ['入库明细']),
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
  schema: useInOrderLineFormSchema((field, row) => {
    switch (field) {
      case 'batchId': {
        snapshot.value.lotNo = row?.lotNo;
        break;
      }
      case 'containerId': {
        snapshot.value.containerNo = row?.containerNo;
        break;
      }
      case 'locId': {
        snapshot.value.locCode = row?.code;
        break;
      }
      case 'matId': {
        snapshot.value.matCode = row?.code;
        snapshot.value.matName = row?.name;
        snapshot.value.matSpec = row?.spec;
        break;
      }
      case 'supplierId': {
        snapshot.value.supplierCode = row?.code;
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
    const values = (await formApi.getValues()) as WmsInOrderApi.InOrderLine;
    // 选批次时批次号快照跟着批次走，没选批次则保留用户手填的到货批次号
    const picked = { ...snapshot.value };
    if (!values.batchId) {
      delete picked.lotNo;
    }
    emit('success', {
      ...formData.value,
      ...values,
      ...picked,
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
    const data = modalApi.getData<WmsInOrderApi.InOrderLine>();
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
