<script lang="ts" setup>
import type { SystemNoticeApi } from '#/api/system/notice';

import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { formatDateTime } from '@vben/utils';

import { getMyNotice } from '#/api/system/notice';
import { DictTag } from '#/components/dict-tag';
import { RichTextView } from '#/components/rich-text-view';

const notice = ref<SystemNoticeApi.Notice>();

const [Modal, modalApi] = useVbenModal({
  showCancelButton: false,
  showConfirmButton: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      notice.value = undefined;
      return;
    }
    // 两种入口：列表点"查看"只给 id，需要回查全文；WebSocket 推送已带全量数据，直接用
    const data = modalApi.getData<{
      id?: number;
      notice?: SystemNoticeApi.Notice;
    }>();
    if (data?.notice) {
      notice.value = data.notice;
      return;
    }
    if (!data?.id) {
      return;
    }
    modalApi.lock();
    try {
      notice.value = await getMyNotice(data.id);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal title="公告详情" class="w-[720px]">
    <div v-if="notice" class="flex flex-col gap-3 px-4 pb-2">
      <div class="flex items-start justify-between gap-4">
        <span class="text-base font-medium">{{ notice.title }}</span>
        <DictTag :type="DICT_TYPE.SYSTEM_NOTICE_TYPE" :value="notice.type" />
      </div>
      <div class="text-xs text-muted-foreground">
        发布于 {{ formatDateTime(notice.createTime) }}
      </div>
      <div class="border-t border-border pt-3">
        <RichTextView :content="notice.content" />
      </div>
    </div>
  </Modal>
</template>
