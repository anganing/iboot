import { computed, effectScope, ref, watch } from 'vue';

import { useAccessStore } from '@vben/stores';

import { useWebSocket } from '@vueuse/core';

/** 消息处理器，收到的是后端 content 反序列化后的对象 */
type MessageHandler = (content: any) => void;

/** 连接成功的处理器，首次连接与每次重连成功都会触发 */
type ConnectedHandler = () => void;

/**
 * 应用级 WebSocket 连接。
 *
 * <p>整个应用只需要一条连接，所以实例放在模块作用域做单例：既避免每个组件各连一条，
 * 也不会像 pinia store 那样被持久化插件序列化或被 resetAllStores 清掉。
 *
 * <p>后端协议见 JsonWebSocketMessageHandler：外层 { type, content }，content 是 JSON 字符串，
 * 心跳走纯文本 ping / pong。
 */
let instance: ReturnType<typeof createAppWebSocket> | undefined;

/**
 * 独立作用域：useWebSocket 默认 autoClose 会把 close 注册到调用方的组件作用域上，
 * 挂在某个组件上会让"谁先调用"决定连接的生命周期。放进游离作用域，生命周期只由引用计数决定。
 */
const scope = effectScope(true);

function createAppWebSocket() {
  const accessStore = useAccessStore();
  // 用 refreshToken 而不是 accessToken：WebSocket 连接无法方便地刷新访问令牌
  const socketUrl = computed(
    () =>
      `${`${import.meta.env.VITE_BASE_URL}/infra/ws`.replace('http', 'ws')}?token=${
        accessStore.refreshToken as string
      }`,
  );

  const messageHandlers = new Map<string, Set<MessageHandler>>();
  const connectedHandlers = new Set<ConnectedHandler>();
  // 引用计数：多个组件都可能 open，最后一个 close 时才真正断开
  const refCount = ref(0);

  /** 分发消息：未注册的类型静默忽略，别的模块的推送不该在这里报错 */
  function dispatch(raw: unknown) {
    if (typeof raw !== 'string' || raw === 'pong') {
      return;
    }
    try {
      const message = JSON.parse(raw) as { content?: string; type?: string };
      if (!message.type) {
        return;
      }
      const handlers = messageHandlers.get(message.type);
      if (!handlers?.size) {
        return;
      }
      const content = message.content ? JSON.parse(message.content) : undefined;
      handlers.forEach((handler) => handler(content));
    } catch (error) {
      console.error('[useAppWebSocket] 解析消息失败', raw, error);
    }
  }

  const {
    close: closeSocket,
    open: openSocket,
    status,
  } = useWebSocket(socketUrl, {
    // 关掉 vueuse 的 url 自动重连：退出登录时 resetAllStores 会把 refreshToken 清空，
    // 它会立刻用 token=null 去连，然后触发十轮重试。改由下面的 watch 自己判断。
    autoConnect: false,
    autoReconnect: { delay: 3000, retries: 10 },
    // 必须显式声明 responseMessage，否则 vueuse 认不出后端回的 pong，
    // 会把它当成业务消息，并因为等不到心跳响应而反复重连
    heartbeat: {
      interval: 30_000,
      message: 'ping',
      pongTimeout: 10_000,
      responseMessage: 'pong',
    },
    immediate: false,
    onConnected() {
      connectedHandlers.forEach((handler) => handler());
    },
    onMessage(_ws, event) {
      dispatch(event.data);
    },
  });

  /** 订阅某类消息，返回取消订阅函数 */
  function on(type: string, handler: MessageHandler) {
    const handlers = messageHandlers.get(type) ?? new Set<MessageHandler>();
    handlers.add(handler);
    messageHandlers.set(type, handlers);
    return () => {
      handlers.delete(handler);
    };
  }

  /**
   * 订阅连接成功事件，返回取消订阅函数。
   *
   * <p>断线期间的推送收不到，业务侧应在这里做一次全量校准。
   */
  function onConnected(handler: ConnectedHandler) {
    connectedHandlers.add(handler);
    return () => {
      connectedHandlers.delete(handler);
    };
  }

  function open() {
    refCount.value += 1;
    // 没有令牌时先不连：连上去也会被握手拦截，白白触发重试。等 watch 到令牌再连
    if (refCount.value === 1 && accessStore.refreshToken) {
      openSocket();
    }
  }

  function close() {
    refCount.value = Math.max(0, refCount.value - 1);
    if (refCount.value === 0) {
      closeSocket();
    }
  }

  // 令牌刷新后要带新 token 重连，但令牌被清空（退出登录）时只断开
  watch(socketUrl, () => {
    if (refCount.value === 0) {
      return;
    }
    closeSocket();
    if (accessStore.refreshToken) {
      openSocket();
    }
  });

  return { close, on, onConnected, open, status };
}

export function useAppWebSocket() {
  if (!instance) {
    instance = scope.run(() => createAppWebSocket())!;
  }
  return instance;
}
