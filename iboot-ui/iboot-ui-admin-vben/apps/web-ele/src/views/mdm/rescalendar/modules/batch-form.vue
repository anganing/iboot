<script lang="ts" setup>
import type { MdmResCalendarApi } from '#/api/mdm/rescalendar';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import { saveResCalendarBatch } from '#/api/mdm/rescalendar';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import { useBatchFormSchema } from '../data';

defineOptions({ name: 'MdmResCalendarBatchForm' });

const emit = defineEmits(['success']);

const formLayout = getMdmFormLayout(1);

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: { class: 'w-full' },
    formItemClass: formLayout.formItemClass,
    labelClass: 'justify-start',
    labelWidth: 100,
  },
  wrapperClass: formLayout.wrapperClass,
  layout: 'horizontal',
  schema: useBatchFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    const values = (await formApi.getValues()) as Record<string, any>;
    const [dateFrom, dateTo] = values.dateRange as [string, string];
    modalApi.lock();
    try {
      const result = await saveResCalendarBatch({
        resIds: values.resIds,
        dateFrom,
        dateTo,
        weekdays: values.weekdays,
        shiftId: values.shiftId,
        availableMinutes: values.availableMinutes,
        remark: values.remark,
      } as MdmResCalendarApi.BatchSaveReq);
      await modalApi.close();
      emit('success');
      ElMessage.success(
        `批量排班完成，新增 ${result.createCount} 条，覆盖 ${result.updateCount} 条`,
      );
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
    // 矩阵传入当前视图的日期区间与勾选资源作为默认值
    const data = modalApi.getData<{
      dateFrom?: string;
      dateTo?: string;
      resIds?: number[];
    }>();
    if (!data) {
      return;
    }
    await formApi.setValues({
      resIds: data.resIds ?? [],
      dateRange:
        data.dateFrom && data.dateTo ? [data.dateFrom, data.dateTo] : undefined,
    });
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" title="批量排班">
    <Form class="mx-4" />
  </Modal>
</template>
