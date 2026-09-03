package com.iboot.pf.module.system.controller.admin.notify.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 站内信的 WebSocket 推送 VO
 *
 * 只带前端铃铛与提醒需要的字段，并附上该用户当前未读数，
 * 这样前端收到推送后不必再回查未读数接口，多标签页的计数也不会各算一套。
 */
@Schema(description = "管理后台 - 站内信推送 Response VO")
@Data
@Accessors(chain = true)
public class NotifyMessagePushRespVO {

    @Schema(description = "站内信编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模版发送人名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "芋艿")
    private String templateNickname;

    @Schema(description = "模版内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "测试内容")
    private String templateContent;

    @Schema(description = "模版类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    private Integer templateType;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

    @Schema(description = "当前用户的未读站内信数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    private Long unreadCount;

}
