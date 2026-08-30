package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 任务依赖新增/修改 Request VO")
@Data
public class TaskDepSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10167")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13102")
    @NotNull(message = "排程版本编号不能为空")
    private Long versionId;

    @Schema(description = "前置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18147")
    @NotNull(message = "前置排程任务编号不能为空")
    private Long fromTaskId;

    @Schema(description = "后置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18828")
    @NotNull(message = "后置排程任务编号不能为空")
    private Long toTaskId;

    @Schema(description = "依赖类型：FINISH_START、START_START、FINISH_FINISH", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "依赖类型：FINISH_START、START_START、FINISH_FINISH不能为空")
    private String depType;

    @Schema(description = "依赖间隔分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "依赖间隔分钟数不能为空")
    private Integer lagMinutes;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}