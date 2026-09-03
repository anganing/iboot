<script lang="ts" setup>
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
  SvgGithubIcon,
} from '@vben/icons';
import {
  BasicLayout,
  Help,
  LockScreen,
  TenantDropdown,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { openWindow } from '@vben/utils';

import { ElMessage } from 'element-plus';

import { getSimpleTenantList } from '#/api/system/tenant';
import { useAppWebSocket } from '#/hooks/use-app-websocket';
import { $t } from '#/locales';
import { router } from '#/router';
import { useAuthStore } from '#/store';
import LoginForm from '#/views/_core/authentication/login.vue';
import {
  MessageCenter,
  useMessageCenter,
} from '#/views/system/message-center';

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
const messageCenter = useMessageCenter();

const [HelpModal, helpModalApi] = useVbenModal({
  connectedComponent: Help,
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

/**
 * 站内信推送：只更新铃铛徽标，不弹浮动提醒。
 *
 * <p>推送提醒会盖住正在操作的页面，长富文本尤其挡视线。消息统一收在铃铛里，
 * 由用户自己决定什么时候看。
 */
function handleNotifyMessagePush(
  content: SystemNotifyMessageApi.NotifyMessage & { unreadCount?: number },
) {
  if (!content?.id) {
    return;
  }
  messageCenter.receiveNotifyPush(content);
}

/** 公告推送：同样只更新徽标，公告条目落到铃铛的通知公告 Tab */
function handleNoticePush(content: SystemNoticeApi.Notice) {
  if (!content?.id) {
    return;
  }
  messageCenter.receiveNoticePush(content);
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
  // 消息改推送驱动：连接成功（含每次重连）后校准两个 Tab 的徽标，补上断线期间漏掉的推送
  socketDisposers.push(
    appWebSocket.onConnected(() => {
      if (userStore.userInfo) {
        messageCenter.syncBadges();
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
      <MessageCenter />
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
</template>
