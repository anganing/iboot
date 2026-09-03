<script lang="ts" setup>
import type { SystemNoticeApi } from '#/api/system/notice';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';
import type { SystemTenantApi } from '#/api/system/tenant';

import {
  computed,
  getCurrentInstance,
  h,
  onBeforeUnmount,
  onMounted,
  ref,
  watch,
} from 'vue';

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
  TenantDropdown,
  UserDropdown,
} from '@vben/layouts';
import { preferences, usePreferences } from '@vben/preferences';
import { useAccessStore, useUserStore } from '@vben/stores';
import { openWindow } from '@vben/utils';

import { ElMessage, ElNotification, ElTooltip } from 'element-plus';

import { getSimpleTenantList } from '#/api/system/tenant';
import { RichTextView } from '#/components/rich-text-view';
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
/**
 * ElNotification 走 render() 脱离组件树渲染，vnode 默认没有 appContext。
 *
 * <p>不把当前应用上下文传进去，富文本正文里的 v-dompurify-html（app.use 注册的全局指令）
 * 解析不到会被静默跳过，提醒里只剩标题和一个空白正文区。
 */
const appContext = getCurrentInstance()?.appContext;

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
 * 提醒正文渲染成富文本。
 *
 * <p>限高加滚动：公告可能带图片和长段落，糊满半屏就没法继续操作页面了。
 */
function renderPushContent(content?: string) {
  return () =>
    h('div', { class: 'max-h-60 overflow-y-auto' }, [
      h(RichTextView, { content }),
    ]);
}

/** 站内信推送：更新铃铛状态，并弹一条能看到完整富文本的提醒 */
function handleNotifyMessagePush(
  content: SystemNotifyMessageApi.NotifyMessage & { unreadCount?: number },
) {
  if (!content?.id) {
    return;
  }
  messageCenter.receiveNotifyPush(content);
  ElNotification(
    {
      duration: 8000,
      message: renderPushContent(content.templateContent),
      onClick: () => messageCenter.openNotifyDetail(content),
      title: content.templateNickname || '新站内信',
      type: 'info',
    },
    appContext,
  );
}

/** 公告推送：只提醒在线用户，点击后在铃铛浮层里看全文 */
function handleNoticePush(content: SystemNoticeApi.Notice) {
  if (!content?.id) {
    return;
  }
  messageCenter.receiveNoticePush(content);
  ElNotification(
    {
      duration: 10_000,
      message: renderPushContent(content.content),
      onClick: () => messageCenter.openNoticeDetail(content),
      title: content.title || '新公告',
      type: 'warning',
    },
    appContext,
  );
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
        messageCenter.loadUnreadCount();
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
</template>
