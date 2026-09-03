<script lang="ts" setup>
import type { NotificationItem } from '@vben/layouts';

import type { SystemNoticeApi } from '#/api/system/notice';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';
import type { SystemTenantApi } from '#/api/system/tenant';

import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';

import { useAccess } from '@vben/access';
import { AuthenticationLoginExpiredModal, useVbenModal } from '@vben/common-ui';
import { VBEN_DOC_URL, VBEN_GITHUB_URL } from '@vben/constants';
import { isTenantEnable, useTabs, useWatermark } from '@vben/hooks';
import {
  AntdProfileOutlined,
  BookOpenText,
  CircleHelp,
  IconifyIcon,
  SvgGithubIcon,
} from '@vben/icons';
import {
  BasicLayout,
  Help,
  LockScreen,
  Notification,
  TenantDropdown,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { formatDateTime, openWindow } from '@vben/utils';

import { ElButton, ElMessage, ElNotification, ElTooltip } from 'element-plus';

import {
  getUnreadNotifyMessageCount,
  getUnreadNotifyMessageList,
  updateAllNotifyMessageRead,
  updateNotifyMessageRead,
} from '#/api/system/notify/message';
import { getSimpleTenantList } from '#/api/system/tenant';
import { RichTextView } from '#/components/rich-text-view';
import { useAppWebSocket } from '#/hooks/use-app-websocket';
import { $t } from '#/locales';
import { router } from '#/router';
import { useAuthStore } from '#/store';
import { htmlToText } from '#/utils';
import LoginForm from '#/views/_core/authentication/login.vue';
import NoticeDetail from '#/views/system/notice/my/modules/detail.vue';
import NotifyMessageDetail from '#/views/system/notify/my/modules/detail.vue';

/** WebSocket 消息类型，与后端 WebSocketMessageTypeConstants 对齐 */
const NOTIFY_MESSAGE_PUSH = 'notify-message-push';
const NOTICE_PUSH = 'notice-push';

const userStore = useUserStore();
const authStore = useAuthStore();
const accessStore = useAccessStore();
const { hasAccessByCodes } = useAccess();
const { destroyWatermark, updateWatermark } = useWatermark();
const { closeOtherTabs, refreshTab } = useTabs();
const appWebSocket = useAppWebSocket();

const notifications = ref<NotificationItem[]>([]);
const unreadCount = ref(0);
const showDot = computed(() => unreadCount.value > 0);

const [HelpModal, helpModalApi] = useVbenModal({
  connectedComponent: Help,
});
const [NotifyDetailModal, notifyDetailModalApi] = useVbenModal({
  connectedComponent: NotifyMessageDetail,
  destroyOnClose: true,
});
const [NoticeDetailModal, noticeDetailModalApi] = useVbenModal({
  connectedComponent: NoticeDetail,
  destroyOnClose: true,
});
const { isDark } = usePreferences();

const menus = computed(() => [
  {
    handler: () => {
      router.push({ name: 'Profile' });
    },
    icon: AntdProfileOutlined,
    text: $t('ui.widgets.profile'),
  },
  {
    handler: () => {
      openWindow(VBEN_DOC_URL, {
        target: '_blank',
      });
    },
    icon: BookOpenText,
    text: $t('ui.widgets.document'),
  },
  {
    handler: () => {
      openWindow(VBEN_GITHUB_URL, {
        target: '_blank',
      });
    },
    icon: SvgGithubIcon,
    text: 'GitHub',
  },
  {
    handler: () => {
      helpModalApi.open();
    },
    icon: CircleHelp,
    text: $t('ui.widgets.qa'),
  },
]);

const avatar = computed(() => {
  return userStore.userInfo?.avatar ?? preferences.app.defaultAvatar;
});

async function handleLogout() {
  await authStore.logout(false);
}

/** 站内信转成铃铛条目，raw 留着给详情弹窗用 */
function toNotificationItem(
  message: SystemNotifyMessageApi.NotifyMessage,
): NotificationItem {
  return {
    avatar: preferences.app.defaultAvatar,
    date: formatDateTime(message.createTime) as string,
    id: message.id,
    isRead: false,
    message: message.templateContent,
    raw: message,
    templateType: message.templateType,
    title: message.templateNickname,
  };
}

/** 获得未读消息数 */
async function handleNotificationGetUnreadCount() {
  unreadCount.value = await getUnreadNotifyMessageCount();
}

/** 获得消息列表 */
async function handleNotificationGetList() {
  const list = await getUnreadNotifyMessageList();
  notifications.value = list.map((item) => toNotificationItem(item));
}

/** 跳转我的站内信 */
function handleNotificationViewAll() {
  router.push({
    name: 'MyNotifyMessage',
  });
}

/** 标记所有已读 */
async function handleNotificationMakeAll() {
  await updateAllNotifyMessageRead();
  unreadCount.value = 0;
  notifications.value = [];
}

/** 清空通知 */
async function handleNotificationClear() {
  await handleNotificationMakeAll();
}

/** 标记单个已读 */
async function handleNotificationRead(item: NotificationItem) {
  if (!item.id) {
    return;
  }
  await updateNotifyMessageRead([item.id]);
  await handleNotificationGetUnreadCount();
  notifications.value = notifications.value.filter((n) => n.id !== item.id);
}

/** 点击某条消息：标记已读并打开详情 */
async function handleNotificationClick(item: NotificationItem) {
  notifyDetailModalApi.setData(item.raw ?? item).open();
  if (!item.isRead) {
    await handleNotificationRead(item);
  }
}

/** 站内信推送：未读数由后端一起带回来，前端不本地自增，多标签页才不会各算一套 */
function handleNotifyMessagePush(
  content: SystemNotifyMessageApi.NotifyMessage & { unreadCount?: number },
) {
  if (!content?.id) {
    return;
  }
  unreadCount.value = content.unreadCount ?? unreadCount.value + 1;
  notifications.value = [
    toNotificationItem(content),
    ...notifications.value.filter((item) => item.id !== content.id),
  ];
  ElNotification({
    duration: 5000,
    message: htmlToText(content.templateContent, 100),
    onClick: () => handleNotificationClick(toNotificationItem(content)),
    title: content.templateNickname || '新站内信',
    type: 'info',
  });
}

/** 公告推送：提醒里直接看全文，错过了还能去「我的公告」页回看 */
function handleNoticePush(content: SystemNoticeApi.Notice) {
  if (!content?.id) {
    return;
  }
  ElNotification({
    duration: 8000,
    message: htmlToText(content.content, 100),
    onClick: () => {
      // 推送已带全量内容，直接开弹窗，不必再跳页回查
      noticeDetailModalApi.setData({ notice: content }).open();
    },
    title: content.title || '新公告',
    type: 'warning',
  });
}

/** 处理通知打开 */
function handleNotificationOpen(open: boolean) {
  if (!open) {
    return;
  }
  handleNotificationGetList();
  handleNotificationGetUnreadCount();
}

/** 打开 IM 聊天 */
function handleOpenImHome() {
  const { href } = router.resolve({ name: 'ImHome' });
  window.open(href, '_blank');
}

// 租户列表
const tenants = ref<SystemTenantApi.Tenant[]>([]);
const tenantEnable = computed(
  () => hasAccessByCodes(['system:tenant:visit']) && isTenantEnable(),
);

/** 获取租户列表 */
async function handleGetTenantList() {
  if (tenantEnable.value) {
    tenants.value = await getSimpleTenantList();
  }
}

/** 处理租户切换 */
async function handleTenantChange(tenant: SystemTenantApi.Tenant) {
  if (!tenant || !tenant.id) {
    ElMessage.error('切换租户失败');
    return;
  }
  // 设置访问租户 ID
  accessStore.setVisitTenantId(tenant.id as number);
  // 关闭其他标签页，只保留当前页
  await closeOtherTabs();
  // 刷新当前页面
  await refreshTab();
  // 提示切换成功
  ElMessage.success(`切换当前租户为: ${tenant.name}`);
}

// ========== 初始化 ==========
/** WebSocket 订阅的取消函数，卸载时逐个释放 */
const socketDisposers: Array<() => void> = [];

onMounted(() => {
  // 获取租户列表
  handleGetTenantList();
  // 站内信改推送驱动：连接成功（含每次重连）后校准一次未读数，补上断线期间漏掉的推送
  socketDisposers.push(
    appWebSocket.onConnected(() => {
      if (userStore.userInfo) {
        handleNotificationGetUnreadCount();
      }
    }),
    appWebSocket.on(NOTIFY_MESSAGE_PUSH, handleNotifyMessagePush),
    appWebSocket.on(NOTICE_PUSH, handleNoticePush),
  );
  appWebSocket.open();
});

onBeforeUnmount(() => {
  socketDisposers.forEach((dispose) => dispose());
  socketDisposers.length = 0;
  appWebSocket.close();
});

watch(
  () => ({
    enable: preferences.app.watermark,
    content: preferences.app.watermarkContent,
    isDark: isDark.value,
  }),
  async ({ enable, content, isDark: isDarkValue }) => {
    if (enable) {
      const watermarkColor = isDarkValue
        ? 'rgba(255, 255, 255, 0.12)'
        : 'rgba(0, 0, 0, 0.12)';

      await updateWatermark({
        advancedStyle: {
          colorStops: [
            {
              color: watermarkColor,
              offset: 0,
            },
            {
              color: watermarkColor,
              offset: 1,
            },
          ],
          type: 'linear',
        },
        content:
          content ||
          `${userStore.userInfo?.id} - ${userStore.userInfo?.nickname}`,
      });
    } else {
      destroyWatermark();
    }
  },
  {
    immediate: true,
  },
);
</script>

<template>
  <BasicLayout @clear-preferences-and-logout="handleLogout">
    <template #user-dropdown>
      <UserDropdown
        :avatar
        :menus
        :text="userStore.userInfo?.nickname"
        :description="userStore.userInfo?.email"
        :tag-text="userStore.userInfo?.username"
        @logout="handleLogout"
        @clear-preferences-and-logout="handleLogout"
      />
    </template>
    <template #notification>
      <Notification
        :dot="showDot"
        :notifications="notifications"
        @clear="handleNotificationClear"
        @make-all="handleNotificationMakeAll"
        @view-all="handleNotificationViewAll"
        @open="handleNotificationOpen"
        @read="handleNotificationRead"
        @on-click="handleNotificationClick"
      >
        <!--
          覆盖 content 插槽以富文本渲染消息内容。
          注意框架层把默认的未读圆点与操作按钮嵌在 content 默认内容里，覆盖后要自己补上。
        -->
        <template #content="{ item }">
          <span
            class="flex size-9 shrink-0 items-center justify-center rounded-full bg-accent text-accent-foreground"
          >
            <IconifyIcon
              class="size-4"
              :icon="
                item.templateType === 1
                  ? 'lucide:megaphone'
                  : 'lucide:bell-ring'
              "
            />
          </span>
          <div class="flex min-w-0 flex-1 flex-col gap-1">
            <p class="truncate font-semibold">{{ item.title }}</p>
            <!-- 富文本内容限高裁切：line-clamp 会把 prose 段落改成 -webkit-box，这里用 max-height -->
            <div class="max-h-10 overflow-hidden">
              <RichTextView
                class="text-xs text-muted-foreground"
                :content="item.message"
              />
            </div>
            <p class="text-xs text-muted-foreground">{{ item.date }}</p>
          </div>
          <div class="flex shrink-0 flex-col items-end gap-1">
            <span
              v-if="!item.isRead"
              class="size-2 rounded-sm bg-primary"
              title="未读"
            ></span>
            <ElButton
              v-if="!item.isRead"
              link
              size="small"
              type="primary"
              @click.stop="handleNotificationRead(item)"
            >
              标记已读
            </ElButton>
          </div>
        </template>
      </Notification>
    </template>
    <template #header-right-1>
      <div v-if="tenantEnable">
        <TenantDropdown
          class="mr-2"
          :tenant-list="tenants"
          :visit-tenant-id="accessStore.visitTenantId"
          @success="handleTenantChange"
        />
      </div>
    </template>
    <template #header-right-900>
      <ElTooltip content="IM 聊天">
        <button
          class="hover:bg-accent hover:text-accent-foreground mr-1 inline-flex size-8 items-center justify-center rounded-md transition-colors"
          type="button"
          @click="handleOpenImHome"
        >
          <IconifyIcon class="size-4" icon="lucide:message-circle" />
        </button>
      </ElTooltip>
    </template>
    <template #extra>
      <AuthenticationLoginExpiredModal
        v-model:open="accessStore.loginExpired"
        :avatar
      >
        <LoginForm />
      </AuthenticationLoginExpiredModal>
    </template>
    <template #lock-screen>
      <LockScreen :avatar @to-login="handleLogout" />
    </template>
  </BasicLayout>
  <HelpModal />
  <NotifyDetailModal />
  <NoticeDetailModal />
</template>
