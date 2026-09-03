package com.iboot.pf.module.system.service.notify;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.enums.UserTypeEnum;
import com.iboot.pf.module.infra.api.websocket.WebSocketSenderApi;
import com.iboot.pf.module.system.controller.admin.notify.vo.message.NotifyMessagePushRespVO;
import com.iboot.pf.module.system.dal.dataobject.notify.NotifyTemplateDO;
import com.iboot.pf.module.system.enums.WebSocketMessageTypeConstants;
import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static com.iboot.pf.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.iboot.pf.module.system.enums.ErrorCodeConstants.*;

/**
 * 站内信发送 Service 实现类
 *
 * @author xrcoder
 */
@Service
@Validated
@Slf4j
public class NotifySendServiceImpl implements NotifySendService {

    @Resource
    private NotifyTemplateService notifyTemplateService;

    @Resource
    private NotifyMessageService notifyMessageService;

    @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
    @Autowired(required = false) // iboot.websocket.enable 可以关掉 WebSocket，这里不能强制注入
    private WebSocketSenderApi webSocketSenderApi;

    @Override
    public Long sendSingleNotifyToAdmin(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.ADMIN.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleNotifyToMember(Long userId, String templateCode, Map<String, Object> templateParams) {
        return sendSingleNotify(userId, UserTypeEnum.MEMBER.getValue(), templateCode, templateParams);
    }

    @Override
    public Long sendSingleNotify(Long userId, Integer userType, String templateCode, Map<String, Object> templateParams) {
        // 校验模版
        NotifyTemplateDO template = validateNotifyTemplate(templateCode);
        if (Objects.equals(template.getStatus(), CommonStatusEnum.DISABLE.getStatus())) {
            log.info("[sendSingleNotify][模版({})已经关闭，无法给用户({}/{})发送]", templateCode, userId, userType);
            return null;
        }
        // 校验参数
        validateTemplateParams(template, templateParams);

        // 发送站内信
        String content = notifyTemplateService.formatNotifyTemplateContent(template.getContent(), templateParams);
        Long messageId = notifyMessageService.createNotifyMessage(userId, userType, template, content, templateParams);

        // 推送给接收人，让前端铃铛实时更新，不必轮询未读数
        executeAfterCommit(() -> pushNotifyMessage(messageId, userId, userType, template, content));
        return messageId;
    }

    /**
     * 通过 WebSocket 把新站内信推给接收人
     *
     * <p>推送是观测手段而非业务数据：WebSocket 未启用、连接不在线、序列化异常都不该影响站内信本身，
     * 所以这里把异常全部收敛成日志。
     */
    private void pushNotifyMessage(Long messageId, Long userId, Integer userType,
                                   NotifyTemplateDO template, String content) {
        if (webSocketSenderApi == null) {
            return;
        }
        try {
            NotifyMessagePushRespVO push = new NotifyMessagePushRespVO().setId(messageId)
                    .setTemplateNickname(template.getNickname()).setTemplateContent(content)
                    .setTemplateType(template.getType()).setCreateTime(LocalDateTime.now())
                    .setUnreadCount(notifyMessageService.getUnreadNotifyMessageCount(userId, userType));
            webSocketSenderApi.sendObject(userType, userId,
                    WebSocketMessageTypeConstants.NOTIFY_MESSAGE_PUSH, push);
        } catch (Exception ex) {
            log.warn("[pushNotifyMessage][messageId({}) userId({}/{}) 推送失败]", messageId, userId, userType, ex);
        }
    }

    /**
     * 事务提交后执行，无事务时直接执行
     *
     * <p>站内信落库通常在业务事务里，提交前推送会让前端拿到还查不到的消息编号。
     */
    private void executeAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

            @Override
            public void afterCommit() {
                task.run();
            }

        });
    }

    @VisibleForTesting
    public NotifyTemplateDO validateNotifyTemplate(String templateCode) {
        // 获得站内信模板。考虑到效率，从缓存中获取
        NotifyTemplateDO template = notifyTemplateService.getNotifyTemplateByCodeFromCache(templateCode);
        // 站内信模板不存在
        if (template == null) {
            throw exception(NOTICE_NOT_FOUND);
        }
        return template;
    }

    /**
     * 校验站内信模版参数是否确实
     *
     * @param template 邮箱模板
     * @param templateParams 参数列表
     */
    @VisibleForTesting
    public void validateTemplateParams(NotifyTemplateDO template, Map<String, Object> templateParams) {
        template.getParams().forEach(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(NOTIFY_SEND_TEMPLATE_PARAM_MISS, key);
            }
        });
    }
}
