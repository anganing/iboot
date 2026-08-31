package com.iboot.pf.module.aps.controller.admin.shortage.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 缺料人工处理 Request VO。
 *
 * <p>缺料的数量与状态由刷新动作按库存投影推导，人工只能补两件事：预计供给时间和“先忽略这条”。
 */
@Schema(description = "管理后台 - APS-排程缺料处理 Request VO")
@Data
public class ShortageHandleReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "缺料编号不能为空")
    private Long id;

    @Schema(description = "预计供给时间")
    private LocalDateTime expectedSupplyDate;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
