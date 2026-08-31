package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.aps.enums.ApsTaskDepTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 排程任务依赖新增/修改 Request VO。
 *
 * <p>版本编号不由前端提交：依赖必须落在前置任务所属的版本上，由后端从任务反推，避免跨版本连边。
 */
@Schema(description = "管理后台 - APS-排程任务依赖新增/修改 Request VO")
@Data
public class TaskDepSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "前置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "前置排程任务不能为空")
    private Long fromTaskId;

    @Schema(description = "后置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "后置排程任务不能为空")
    private Long toTaskId;

    @Schema(description = "依赖类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "FINISH_START")
    @NotEmpty(message = "依赖类型不能为空")
    @InEnum(ApsTaskDepTypeEnum.class)
    private String depType;

    @Schema(description = "依赖间隔分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "依赖间隔分钟数不能为空")
    @Min(value = 0, message = "依赖间隔分钟数不能小于零")
    private Integer lagMinutes;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
