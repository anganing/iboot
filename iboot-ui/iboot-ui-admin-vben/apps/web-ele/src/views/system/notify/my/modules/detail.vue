<script lang="ts" setup>
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';
import { RichTextView } from '#/components/rich-text-view';

const formData = ref<SystemNotifyMessageApi.NotifyMessage>();

const [Modal, modalApi] = useVbenModal({
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<SystemNotifyMessageApi.NotifyMessage>();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = data;
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    title="站内信详情"
    class="w-[720px]"
    :show-cancel-button="false"
    :show-confirm-button="false"
  >
    <div v-if="formData" class="flex flex-col gap-3 px-4 pb-2">
      <div class="flex items-center justify-between gap-4">
        <span class="text-base font-medium">
          {{ formData.templateNickname }}
        </span>
        <DictTag
          :type="DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE"
          :value="formData.templateType"
        />
      </div>
      <div class="text-xs text-muted-foreground">
        发送于 {{ formatDateTime(formData.createTime) }}
      </div>
      <div class="border-t border-border pt-3">
        <RichTextView :content="formData.templateContent" />
      </div>
      <div v-if="formData.readTime" class="text-xs text-muted-foreground">
        已读于 {{ formatDateTime(formData.readTime) }}
      </div>
    </div>
  </Modal>
</template>
