<script lang="ts" setup>
import type { MesOpTaskApi } from '#/api/mes/optask';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  finishOpTask,
  pauseOpTask,
  resumeOpTask,
  startOpTask,
} from '#/api/mes/optask';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import {
  useOpTaskActionFormSchema,
  useOpTaskFinishFormSchema,
} from '../data';

const emit = defineEmits(['success']);

type Action = 'start' | 'pause' | 'resume' | 'finish';

const action = ref<Action>('start');
const task = ref<MesOpTaskApi.OpTask>();
const titles: Record<Action, string> = {
  start: '工序开工',
  pause: '工序暂停',
  resume: '工序恢复',
  finish: '工序完工',
};
const getTitle = computed(() => titles[action.value]);
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
  schema: useOpTaskActionFormSchema('start'),
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
      const values = await formApi.getValues();
      if (action.value === 'start') {
        await startOpTask({
          id: task.value!.id!,
          operatorId: values.operatorId,
          reason: values.reason,
        });
      } else if (action.value === 'pause') {
        await pauseOpTask({ id: task.value!.id!, reason: values.reason });
      } else if (action.value === 'resume') {
        await resumeOpTask({ id: task.value!.id! });
      } else {
        await finishOpTask({
          id: task.value!.id!,
          qualifiedQty: values.qualifiedQty,
          scrapQty: values.scrapQty,
          reason: values.reason,
        });
      }
      await modalApi.close();
      emit('success');
      ElMessage.success('操作成功');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      task.value = undefined;
      return;
    }
    const data = modalApi.getData<{
      action: Action;
      row: MesOpTaskApi.OpTask;
    }>();
    action.value = data.action;
    task.value = data.row;
    await formApi.resetForm();
    const schema =
      action.value === 'finish'
        ? useOpTaskFinishFormSchema()
        : useOpTaskActionFormSchema(action.value === 'pause' ? 'pause' : 'start');
    formApi.setState({ schema });
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
