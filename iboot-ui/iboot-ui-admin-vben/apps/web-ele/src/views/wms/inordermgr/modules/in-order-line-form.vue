<script lang="ts" setup>
import type { WmsInOrderApi } from '#/api/wms/inordermgr';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElButton } from 'element-plus';

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
/** 连续录入时保留的字段：物料相关与库位、容器、供应商这类逐行重复的取值 */
const STICKY_FIELDS = [
  'matId',
  'locId',
  'containerId',
  'supplierId',
] as const;

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
        // 物料主数据自带基准计量单位，选完就带出来，省掉每行手选一次
        if (row?.baseUomId) {
          snapshot.value.uomCode = row?.baseUomCode;
          formApi.setFieldValue('uomId', row.baseUomId);
        }
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

/** 取当前表单值并合上选择器快照，得到一行完整明细 */
async function collectLine() {
  const values = (await formApi.getValues()) as WmsInOrderApi.InOrderLine;
  // 选批次时批次号快照跟着批次走，没选批次则保留用户手填的到货批次号
  const picked = { ...snapshot.value };
  if (!values.batchId) {
    delete picked.lotNo;
  }
  return {
    ...formData.value,
    ...values,
    ...picked,
  } as WmsInOrderApi.InOrderLine;
}

/**
 * 保存并继续录入下一行。
 *
 * 一行一个弹窗，连录时最烦的是把物料、库位、容器、供应商这些逐行重复的取值重填一遍。
 * 这里保留它们（连同快照一起），只清掉数量、批次、质量状态和备注——数量每行必不同，
 * 批次跟着物料走不会重复，质量状态默认合格已足够。父弹窗收到 success 后照常入表，
 * 本弹窗不关，光标留在数量上等下一行。
 */
async function handleSaveAndNext() {
  const { valid } = await formApi.validateField('planQty');
  if (!valid) {
    return;
  }
  emit('success', await collectLine());
  const sticky = (await formApi.getValues()) as Record<string, any>;
  const kept: Record<string, any> = {};
  for (const field of STICKY_FIELDS) {
    kept[field] = sticky[field];
  }
  await formApi.resetForm();
  await formApi.setValues(kept);
  // resetForm 清空表单值但不动 snapshot 之外的部分，物料快照要留着才能继续显示编码
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    emit('success', await collectLine());
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
    <template #prepend-footer>
      <!-- 「继续录入」是新增才有的动作：编辑态点它语义说不通，且 formData 带着原行标识，
           会在父组件走覆盖分支，看着像新增实为改行 -->
      <div v-if="!formData" class="flex flex-auto items-center">
        <ElButton @click="handleSaveAndNext"> 保存并继续录入 </ElButton>
      </div>
    </template>
  </Modal>
</template>
