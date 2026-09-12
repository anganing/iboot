package com.iboot.pf.module.infra.api.websocket;

import com.iboot.pf.framework.datapermission.core.annotation.DataPermission;
import com.iboot.pf.framework.websocket.core.sender.WebSocketMessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * WebSocket 发送器的 API 实现类
 *
 * <p>类上一个 {@code @DataPermission(enable = false)} 兜住全部方法：门面是给别的模块调的，
 * 给谁推哪条消息与各业务模块登录用户的数据范围毫无关系，跨模块调用天生是系统身份而不是用户身份。
 * 打在类上而不是逐方法加，是为了不出现"加了一半"的漏洞。
 *
 * @author iboot
 */
@Component
@DataPermission(enable = false)
public class WebSocketSenderApiImpl implements WebSocketSenderApi {

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // 由于 iboot.websocket.enable 配置项，可以关闭 WebSocket 的功能，所以这里只能不强制注入
    private WebSocketMessageSender webSocketMessageSender;

    @Override
    public void send(Integer userType, Long userId, String messageType, String messageContent) {
        webSocketMessageSender.send(userType, userId, messageType, messageContent);
    }

    @Override
    public void send(Integer userType, String messageType, String messageContent) {
        webSocketMessageSender.send(userType, messageType, messageContent);
    }

    @Override
    public void send(String sessionId, String messageType, String messageContent) {
        webSocketMessageSender.send(sessionId, messageType, messageContent);
    }

}
