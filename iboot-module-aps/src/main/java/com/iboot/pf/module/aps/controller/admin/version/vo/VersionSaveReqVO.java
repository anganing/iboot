package com.iboot.pf.module.aps.controller.admin.version.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 排程版本新增/修改 Request VO")
@Data
public class VersionSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2759")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17546")
    @NotNull(message = "排程场景编号不能为空")
    private Long scenarioId;

    @Schema(description = "排程版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排程版本号不能为空")
    private Integer versionNo;

    @Schema(description = "状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：SOLVING、SOLVED、PENDING_CONFIRM、PUBLISHED、REPLACED、FAILED不能为空")
    private String status;

    @Schema(description = "Timefold综合得分")
    private String score;

    @Schema(description = "硬约束得分")
    private Long hardScore;

    @Schema(description = "软约束得分")
    private Long softScore;

    @Schema(description = "求解开始时间")
    private LocalDateTime solveStartTime;

    @Schema(description = "求解结束时间")
    private LocalDateTime solveEndTime;

    @Schema(description = "发布时间")
    private LocalDateTime publishedTime;

    @Schema(description = "发布人员编号")
    private Long publishedBy;

    @Schema(description = "求解结果或失败信息")
    private String solverMessage;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}