<script lang="ts" setup>
import type { MdmRouteOpEsopApi } from '#/api/mdm/routeopesop';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElMessage } from 'element-plus';

import { useVbenForm } from '#/adapter/form';
import {
  createRouteOpEsop,
  getRouteOpEsop,
  updateRouteOpEsop,
} from '#/api/mdm/routeopesop';
import { $t } from '#/locales';
import { getMdmFormLayout } from '#/views/mdm/form-layout';

import RouteOpEsopFileField from './route-op-esop-file-field.vue';

const emit = defineEmits(['success']);
const formData = ref<Partial<MdmRouteOpEsopApi.RouteOpEsop>>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['ESOP 作业步骤'])
    : $t('ui.actionTitle.create', ['ESOP 作业步骤']);
});

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
  schema: [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'seq',
      label: '展示顺序',
      rules: 'required',
      component: 'InputNumber',
      componentProps: {
        class: '!w-full',
        controlsPosition: 'right',
        min: 0,
        max: 9999,
        precision: 0,
      },
      defaultValue: 10,
    },
    {
      fieldName: 'title',
      label: '标题',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标题',
        maxLength: 128,
      },
    },
    {
      fieldName: 'content',
      label: '作业要点',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入作业要点说明',
        rows: 5,
      },
    },
    {
      fieldName: 'paramJson',
      label: '工艺参数',
      component: 'Textarea',
      componentProps: {
        placeholder: '工艺参数 JSON，例如 {"温度": "260±5℃", "时间": "3s"}',
        rows: 4,
      },
    },
    {
      fieldName: 'files',
      label: '附件',
      component: RouteOpEsopFileField,
      componentProps: {
        class: 'w-full',
      },
      defaultValue: [],
    },
    {
      fieldName: 'requiredConfirmFlag',
      label: '要求确认阅读',
      component: 'Switch',
      componentProps: {
        checkedValue: true,
        uncheckedValue: false,
      },
      defaultValue: false,
    },
    {
      fieldName: 'status',
      label: '状态',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
      defaultValue: 0,
    },
  ],
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid || !formData.value?.routeOpId) {
      return;
    }
    const data = (await formApi.getValues()) as MdmRouteOpEsopApi.RouteOpEsop;
    data.routeOpId = formData.value.routeOpId;
    // 附件只回传后端 ReqVO 认识的字段，seq 与 fileType 由后端按顺序和扩展名生成
    data.files = (data.files ?? []).map((file) => ({
      fileName: file.fileName,
      fileUrl: file.fileUrl,
      fileId: file.fileId,
    }));
    modalApi.lock();
    try {
      await (formData.value?.id
        ? updateRouteOpEsop(data)
        : createRouteOpEsop(data));
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
      return;
    }
    await formApi.resetForm();
    const data = modalApi.getData<MdmRouteOpEsopApi.RouteOpEsop>();
    formData.value = data ?? {};
    if (!data?.id) {
      await formApi.setValues(formData.value);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getRouteOpEsop(data.id);
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :class="formLayout.modalClass" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
