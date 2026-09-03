package com.iboot.pf.module.system.enums;

/**
 * system 模块的 WebSocket 消息类型常量
 *
 * 这里的值是前后端的通信协议，前端 use-app-websocket 按此分发消息，
 * 所以只能在这里定义，不允许在 Controller / Service 里写字面量。
 *
 * @author iboot
 */
public interface WebSocketMessageTypeConstants {

    /**
     * 站内信推送：有新的站内信时，定向推送给接收人
     */
    String NOTIFY_MESSAGE_PUSH = "notify-message-push";

    /**
     * 通知公告推送：只推送给在线用户，不落库
     */
    String NOTICE_PUSH = "notice-push";

}
