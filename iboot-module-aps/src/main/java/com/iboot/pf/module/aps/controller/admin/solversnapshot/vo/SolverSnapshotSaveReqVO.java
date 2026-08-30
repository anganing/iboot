package com.iboot.pf.module.aps.controller.admin.solversnapshot.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-Timefold求解快照新增/修改 Request VO")
@Data
public class SolverSnapshotSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "2308")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26936")
    @NotNull(message = "排程版本编号不能为空")
    private Long versionId;

    @Schema(description = "快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "快照类型：INPUT、SOLUTION、CONSTRAINT_MATCH、ERROR不能为空")
    private String snapshotType;

    @Schema(description = "Timefold求解输入、结果或约束命中快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Timefold求解输入、结果或约束命中快照不能为空")
    private String snapshotJson;

    @Schema(description = "快照内容校验值", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "快照内容校验值不能为空")
    private String checksum;

    @Schema(description = "快照生成时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "快照生成时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}