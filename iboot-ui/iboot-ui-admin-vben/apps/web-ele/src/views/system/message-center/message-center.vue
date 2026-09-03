<script lang="ts" setup>
import type { MessageKind } from './use-message-center';

import type { SystemNoticeApi } from '#/api/system/notice';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';

import { computed, watch } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatDateTime } from '@vben/utils';

import {
  ElBadge,
  ElButton,
  ElEmpty,
  ElPopover,
  ElScrollbar,
  ElTabPane,
  ElTabs,
  ElTooltip,
} from 'element-plus';

import { DictTag } from '#/components/dict-tag';
import { RichTextView } from '#/components/rich-text-view';
import { router } from '#/router';
import { htmlToText } from '#/utils';

import { useMessageCenter } from './use-message-center';

const messageCenter = useMessageCenter();
const {
  activeKind,
  detail,
  noticeList,
  noticeLoading,
  notifyList,
  popoverVisible,
  unreadCount,
} = messageCenter;

/** 未读数超过 99 就不再显示具体数字，避免把徽标撑变形 */
const badgeValue = computed(() =>
  unreadCount.value > 99 ? '99+' : unreadCount.value,
);

/** 打开时按当前 Tab 拉数据；公告 Tab 首次切换时才请求 */
watch(popoverVisible, (visible) => {
  if (!visible) {
    messageCenter.closeDetail();
    return;
  }
  messageCenter.loadUnreadCount();
  messageCenter.loadNotifyList();
  if (activeKind.value === 'notice') {
    messageCenter.loadNoticeList();
  }
});

function handleTabChange(name: unknown) {
  messageCenter.closeDetail();
  if (name === 'notice') {
    messageCenter.loadNoticeList();
  }
}

function handleNotifyClick(message: SystemNotifyMessageApi.NotifyMessage) {
  messageCenter.openNotifyDetail(message);
}

function handleNoticeClick(notice: SystemNoticeApi.Notice) {
  messageCenter.openNoticeDetail(notice);
}

async function handleMarkRead(
  message: SystemNotifyMessageApi.NotifyMessage,
  event: MouseEvent,
) {
  event.stopPropagation();
  if (message.id) {
    await messageCenter.markRead(message.id);
  }
}

/** 跳页前收起浮层，否则浮层会盖在新页面上 */
function navigate(name: string) {
  popoverVisible.value = false;
  router.push({ name });
}

function handleViewAll(kind: MessageKind) {
  navigate(kind === 'notify' ? 'MyNotifyMessage' : 'MyNotice');
}
</script>

<template>
  <ElPopover
    v-model:visible="popoverVisible"
    :show-arrow="false"
    :width="400"
    placement="bottom-end"
    popper-class="message-center-popper"
    trigger="click"
  >
    <template #reference>
      <div class="mr-2 flex h-full items-center">
        <ElTooltip content="消息中心" placement="bottom">
          <button
            class="relative inline-flex size-8 items-center justify-center rounded-md transition-colors hover:bg-accent hover:text-accent-foreground"
            type="button"
          >
            <ElBadge
              :hidden="unreadCount === 0"
              :value="badgeValue"
              class="flex items-center"
            >
              <IconifyIcon class="size-4" icon="lucide:bell" />
            </ElBadge>
          </button>
        </ElTooltip>
      </div>
    </template>

    <!-- 详情态：同一浮层内切换，不跳页也不再弹独立弹窗 -->
    <div v-if="detail" class="flex h-100 flex-col">
      <div class="flex items-center gap-2 border-b border-border px-3 py-2">
        <ElButton link @click="messageCenter.closeDetail()">
          <IconifyIcon class="size-4" icon="lucide:arrow-left" />
        </ElButton>
        <span class="flex-1 truncate text-sm font-medium">
          {{ detail.title }}
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
      <ElScrollbar class="min-h-0 flex-1">
        <div class="px-3 py-2">
          <p v-if="detail.time" class="mb-2 text-xs text-muted-foreground">
            {{ detail.kind === 'notice' ? '发布于' : '发送于' }}
            {{ formatDateTime(detail.time) }}
          </p>
          <RichTextView :content="detail.content" />
        </div>
      </ElScrollbar>
    </div>

    <!-- 列表态 -->
    <div v-else class="flex h-100 flex-col">
      <div
        class="flex items-center justify-between border-b border-border px-3 py-2"
      >
        <span class="text-sm font-medium">消息中心</span>
        <ElTooltip
          v-if="activeKind === 'notify'"
          content="全部标记为已读"
          placement="left"
        >
          <ElButton
            :disabled="notifyList.length === 0"
            link
            @click="messageCenter.markAllRead()"
          >
            <IconifyIcon class="size-4" icon="lucide:mail-check" />
          </ElButton>
        </ElTooltip>
      </div>

      <ElTabs
        v-model="activeKind"
        class="message-center-tabs flex-1"
        @tab-change="handleTabChange"
      >
        <ElTabPane label="站内信" name="notify">
          <ElScrollbar height="100%">
            <ElEmpty
              v-if="notifyList.length === 0"
              :image-size="60"
              description="暂无未读站内信"
            />
            <ul v-else class="flex flex-col">
              <li
                v-for="item in notifyList"
                :key="item.id"
                class="flex cursor-pointer items-start gap-3 border-b border-border px-3 py-2.5 last:border-b-0 hover:bg-accent"
                @click="handleNotifyClick(item)"
              >
                <span
                  class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full bg-accent text-accent-foreground"
                >
                  <IconifyIcon class="size-4" icon="lucide:bell-ring" />
                </span>
                <div class="flex min-w-0 flex-1 flex-col gap-0.5">
                  <div class="flex items-center gap-2">
                    <span class="truncate text-sm font-medium">
                      {{ item.templateNickname }}
                    </span>
                    <span
                      v-if="!item.readStatus"
                      class="size-1.5 shrink-0 rounded-full bg-primary"
                    ></span>
                  </div>
                  <p class="line-clamp-2 text-xs text-muted-foreground">
                    {{ htmlToText(item.templateContent) }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ formatDateTime(item.createTime) }}
                  </p>
                </div>
                <ElTooltip content="标记已读" placement="left">
                  <ElButton
                    v-if="!item.readStatus"
                    class="shrink-0"
                    link
                    @click="handleMarkRead(item, $event)"
                  >
                    <IconifyIcon class="size-4" icon="lucide:check" />
                  </ElButton>
                </ElTooltip>
              </li>
            </ul>
          </ElScrollbar>
        </ElTabPane>

        <ElTabPane label="通知公告" name="notice">
          <ElScrollbar height="100%">
            <ElEmpty
              v-if="!noticeLoading && noticeList.length === 0"
              :image-size="60"
              description="暂无公告"
            />
            <ul v-else class="flex flex-col">
              <li
                v-for="item in noticeList"
                :key="item.id"
                class="flex cursor-pointer items-start gap-3 border-b border-border px-3 py-2.5 last:border-b-0 hover:bg-accent"
                @click="handleNoticeClick(item)"
              >
                <span
                  class="mt-0.5 flex size-8 shrink-0 items-center justify-center rounded-full bg-accent text-accent-foreground"
                >
                  <IconifyIcon class="size-4" icon="lucide:megaphone" />
                </span>
                <div class="flex min-w-0 flex-1 flex-col gap-0.5">
                  <div class="flex items-center gap-2">
                    <span class="truncate text-sm font-medium">
                      {{ item.title }}
                    </span>
                    <DictTag
                      :type="DICT_TYPE.SYSTEM_NOTICE_TYPE"
                      :value="item.type"
                    />
                  </div>
                  <p class="line-clamp-2 text-xs text-muted-foreground">
                    {{ htmlToText(item.content) }}
                  </p>
                  <p class="text-xs text-muted-foreground">
                    {{ formatDateTime(item.createTime) }}
                  </p>
                </div>
              </li>
            </ul>
          </ElScrollbar>
        </ElTabPane>
      </ElTabs>

      <div
        class="flex items-center border-t border-border px-3 py-2"
        :class="activeKind === 'notify' ? 'justify-between' : 'justify-end'"
      >
        <template v-if="activeKind === 'notify'">
          <ElButton
            :disabled="notifyList.length === 0"
            link
            @click="messageCenter.markAllRead()"
          >
            清空
          </ElButton>
          <ElButton size="small" type="primary" @click="handleViewAll('notify')">
            查看所有站内信
          </ElButton>
        </template>
        <template v-else>
          <ElButton size="small" type="primary" @click="handleViewAll('notice')">
            查看全部公告
          </ElButton>
        </template>
      </div>
    </div>
  </ElPopover>
</template>

<style lang="scss" scoped>
/*
 * 浮层自带 12px 内边距，会把头部、分割线和页脚整块内缩，面板边缘对不齐；
 * 内部已经按区域给了 px-3/py-2，这里把 popper 的 padding 归零。
 */
:global(.message-center-popper.el-popover.el-popper) {
  padding: 0;
  overflow: hidden;
}

/* Tab 内容区要能撑满并让滚动条落在列表上，否则整个浮层会被长列表顶高 */
.message-center-tabs {
  display: flex;
  flex-direction: column;
  min-height: 0;

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 12px;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}
</style>
