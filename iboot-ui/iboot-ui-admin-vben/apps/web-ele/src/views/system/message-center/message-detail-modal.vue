<script lang="ts" setup>
import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { formatDateTime } from '@vben/utils';

import { DictTag } from '#/components/dict-tag';
import { RichTextView } from '#/components/rich-text-view';

import { useMessageCenter } from './use-message-center';

/**
 * 消息详情弹窗。
 *
 * <p>站内信与公告详情的唯一展示入口：铃铛浮层只有 400px 宽，带图片的长公告在里面读不下，
 * 点条目一律收起浮层来这里看。全屏按钮由 useVbenModal 自带。
 */
const messageCenter = useMessageCenter();
const { detail } = messageCenter;

const [Modal] = useVbenModal({
  // 关窗动画播完再清 detail，关闭过程中内容不会先塌成空白
  onClosed() {
    messageCenter.closeDetail();
  },
  showCancelButton: false,
  showConfirmButton: false,
});
</script>

<template>
  <!-- 不设 content 限高：弹窗自身已有 max-h 与内容区滚动，写死高度会让全屏态下方留白 -->
  <Modal :title="detail?.title" class="w-[860px]">
    <div v-if="detail" class="flex flex-col gap-3 px-4 pb-2">
      <div class="flex items-center gap-3">
        <span class="text-xs text-muted-foreground">
          {{ detail.kind === 'notice' ? '发布于' : '发送于' }}
          {{ formatDateTime(detail.time) }}
        </span>
        <DictTag
          v-if="detail.tagValue !== undefined"
          :type="
            detail.kind === 'notify'
              ? DICT_TYPE.SYSTEM_NOTIFY_TEMPLATE_TYPE
              : DICT_TYPE.SYSTEM_NOTICE_TYPE
          "
          :value="detail.tagValue"
        />
      </div>
      <div class="border-t border-border pt-3">
        <RichTextView :content="detail.content" />
      </div>
    </div>
  </Modal>
</template>
