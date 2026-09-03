import type { SystemNoticeApi } from '#/api/system/notice';
import type { SystemNotifyMessageApi } from '#/api/system/notify/message';

import { computed, ref } from 'vue';

import {
  getMyNotice as getMyNoticeDetail,
  getMyNoticePage,
} from '#/api/system/notice';
import {
  getUnreadNotifyMessageCount,
  getUnreadNotifyMessageList,
  updateAllNotifyMessageRead,
  updateNotifyMessageRead,
} from '#/api/system/notify/message';

/** 消息种类：站内信与通知公告分两个 Tab */
export type MessageKind = 'notice' | 'notify';

/** 详情态，浮层与放大弹窗共用同一份结构 */
export interface MessageDetail {
  content?: string;
  kind: MessageKind;
  tagValue?: number;
  /** 站内信的发送时间 / 公告的发布时间 */
  time?: Date;
  title: string;
}

/** 公告 Tab 一次取 10 条最新的，更多走「查看全部公告」页 */
const NOTICE_PAGE_SIZE = 10;

/**
 * 已看过的最大公告编号，存本地。
 *
 * <p>公告是广播的，库里没有逐用户已读状态，服务端算不出"你有几条没看的"。
 * 记住看过的最大编号，比它新的就算未读，刷新页面后徽标也还在。
 */
const NOTICE_SEEN_ID_KEY = 'message-center-notice-seen-id';

let instance: ReturnType<typeof createMessageCenter> | undefined;

/**
 * 消息中心状态。
 *
 * <p>做成模块作用域单例（与 use-app-websocket 同形态）：铃铛组件与 basic.vue 的
 * WebSocket 推送处理器都要读写这份状态，放 store 里反而会被持久化插件带上。
 */
function createMessageCenter() {
  const activeKind = ref<MessageKind>('notify');
  const notifyList = ref<SystemNotifyMessageApi.NotifyMessage[]>([]);
  const noticeList = ref<SystemNoticeApi.Notice[]>([]);
  const unreadCount = ref(0);
  const detail = ref<MessageDetail>();
  const noticeLoading = ref(false);
  /** 浮层展开态：放大查看时要能把浮层收起来 */
  const popoverVisible = ref(false);
  const noticeSeenId = ref(
    Number(localStorage.getItem(NOTICE_SEEN_ID_KEY)) || 0,
  );

  /** 公告未读数：比"已看过的最大编号"新的都算未读 */
  const noticeUnreadCount = computed(
    () =>
      noticeList.value.filter((item) => (item.id ?? 0) > noticeSeenId.value)
        .length,
  );

  /** 铃铛徽标：两个 Tab 的未读数之和 */
  const unreadTotal = computed(() => unreadCount.value + noticeUnreadCount.value);

  /** 拉未读数：连接建立与重连后用它校准 */
  async function loadUnreadCount() {
    unreadCount.value = await getUnreadNotifyMessageCount();
  }

  /** 拉未读站内信列表 */
  async function loadNotifyList() {
    notifyList.value = await getUnreadNotifyMessageList();
  }

  /** 拉最新的开启中公告 */
  async function loadNoticeList() {
    noticeLoading.value = true;
    try {
      const page = await getMyNoticePage({
        pageNo: 1,
        pageSize: NOTICE_PAGE_SIZE,
      });
      noticeList.value = page.list ?? [];
    } finally {
      noticeLoading.value = false;
    }
  }

  /**
   * 校准两个 Tab 的徽标。
   *
   * <p>连接建立与重连后调用：推送只覆盖在线时段，断线期间发的站内信和公告要靠这里补回来。
   */
  async function syncBadges() {
    await Promise.all([loadUnreadCount(), loadNoticeList()]);
  }

  /** 把当前列表里的公告都记成看过，清掉公告徽标 */
  function markNoticeSeen() {
    const maxId = Math.max(
      noticeSeenId.value,
      ...noticeList.value.map((item) => item.id ?? 0),
    );
    if (maxId === noticeSeenId.value) {
      return;
    }
    noticeSeenId.value = maxId;
    localStorage.setItem(NOTICE_SEEN_ID_KEY, String(maxId));
  }

  /** 标记单条已读，并同步未读数与列表 */
  async function markRead(id: number) {
    await updateNotifyMessageRead([id]);
    notifyList.value = notifyList.value.filter((item) => item.id !== id);
    await loadUnreadCount();
  }

  /** 全部标记已读 */
  async function markAllRead() {
    await updateAllNotifyMessageRead();
    notifyList.value = [];
    unreadCount.value = 0;
  }

  /** 打开站内信详情：进详情顺带标记已读 */
  async function openNotifyDetail(
    message: SystemNotifyMessageApi.NotifyMessage,
  ) {
    activeKind.value = 'notify';
    detail.value = {
      content: message.templateContent,
      kind: 'notify',
      tagValue: message.templateType,
      time: message.createTime,
      title: message.templateNickname || '站内信',
    };
    if (message.id && !message.readStatus) {
      await markRead(message.id);
    }
  }

  /**
   * 打开公告详情。
   *
   * <p>列表条目已带全文，WebSocket 推送也带；只有拿不到内容时才回查一次。
   */
  async function openNoticeDetail(notice: SystemNoticeApi.Notice) {
    activeKind.value = 'notice';
    let target = notice;
    if (!target.content && target.id) {
      target = await getMyNoticeDetail(target.id);
    }
    detail.value = {
      content: target.content,
      kind: 'notice',
      tagValue: target.type,
      time: target.createTime,
      title: target.title || '通知公告',
    };
  }

  /** 退出详情态回到列表 */
  function closeDetail() {
    detail.value = undefined;
  }

  /** 站内信推送：未读数由后端一并带回，前端不本地自增，多标签页才不会各算一套 */
  function receiveNotifyPush(
    message: SystemNotifyMessageApi.NotifyMessage & { unreadCount?: number },
  ) {
    unreadCount.value = message.unreadCount ?? unreadCount.value + 1;
    notifyList.value = [
      message,
      ...notifyList.value.filter((item) => item.id !== message.id),
    ];
  }

  /** 公告推送：只提醒在线用户，不落库，这里只把它插到 Tab 列表顶部 */
  function receiveNoticePush(notice: SystemNoticeApi.Notice) {
    noticeList.value = [
      notice,
      ...noticeList.value.filter((item) => item.id !== notice.id),
    ];
  }

  return {
    activeKind,
    closeDetail,
    detail,
    loadNoticeList,
    loadNotifyList,
    loadUnreadCount,
    markAllRead,
    markNoticeSeen,
    markRead,
    noticeList,
    noticeLoading,
    noticeUnreadCount,
    notifyList,
    openNoticeDetail,
    openNotifyDetail,
    popoverVisible,
    receiveNoticePush,
    receiveNotifyPush,
    syncBadges,
    unreadCount,
    unreadTotal,
  };
}

export function useMessageCenter() {
  if (!instance) {
    instance = createMessageCenter();
  }
  return instance;
}
