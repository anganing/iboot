<script lang="ts" setup>
import type { WmsCountOrderApi } from '#/api/wms/countorder';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElAlert, ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createCountOrder,
  getCountOrder,
  updateCountOrder,
} from '#/api/wms/countorder';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import {
  newCreateIdempotencyKey,
  WMS_ORDER_STATUS,
  zeroIfBlank,
} from '../../order-form';
import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<WmsCountOrderApi.CountOrder>();
/** 创建幂等键，弹窗打开时生成一次，提交失败重试复用同一个键，避免建出第二张单 */
const createKey = ref<string>();

const isEdit = computed(() => Boolean(formData.value?.id));
const getTitle = computed(() =>
  isEdit.value
    ? $t('ui.actionTitle.edit', ['盘点单'])
    : $t('ui.actionTitle.create', ['盘点单']),
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
    try {
      const values = (await formApi.getValues()) as WmsCountOrderApi.CountOrder;
      const data: WmsCountOrderApi.CountOrder = {
        ...values,
        // 库区、项目留空表示"全部库区""不限项目"，但请求 VO 上两个字段是 @NotNull，0 才是那个语义
        zoneId: zeroIfBlank(values.zoneId) as number,
        projectId: zeroIfBlank(values.projectId) as number,
        // 状态与快照时间是请求 VO 上的死字段：领域 Service 把状态写死为新建、快照时间按生成
        // 盘点行的时刻重取。这里只为满足必填校验补值，界面上不给用户填
        status: WMS_ORDER_STATUS.DRAFT,
        snapshotTime: Date.now(),
        // 幂等键：`createCountOrder` 开头按它查重并返回原单号，键不稳定等于同一张单能建两次。
        // 改单时沿用原键，不生成新的
        idempotencyKey: formData.value?.id
          ? formData.value.idempotencyKey
          : createKey.value,
      };
      await (formData.value?.id
        ? updateCountOrder(data)
        : createCountOrder(data));
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
      createKey.value = undefined;
      return;
    }
    await formApi.resetForm();
    const data = modalApi.getData<WmsCountOrderApi.CountOrder>();
    if (!data || !data.id) {
      // 新建：幂等键在这里生成一次，之后每次重试都用它
      createKey.value = newCreateIdempotencyKey('COUNT_ORDER');
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getCountOrder(data.id);
      // 设置到 values
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
      v-if="isEdit"
      class="mx-4 mb-2"
      :closable="false"
      show-icon
      title="保存会按盘点范围重新生成盘点行"
      type="warning"
    >
      盘点物料范围不落库，回显不出原来选的物料：留空保存等于按全盘重新圈选库存余额。
    </ElAlert>
    <Form class="mx-4" />
  </Modal>
</template>
