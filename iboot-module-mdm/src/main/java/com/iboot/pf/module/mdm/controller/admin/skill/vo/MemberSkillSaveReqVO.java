package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 人员技能新增/修改 Request VO")
@Data
public class MemberSkillSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "system 用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "用户不能为空")
    private Long userId;

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "技能不能为空")
    private Long skillId;

    @Schema(description = "技能等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "技能等级不能为空")
    @Min(value = 1, message = "技能等级必须大于 0")
    @Max(value = 99, message = "技能等级不能大于 99")
    private Integer level;

    @Schema(description = "生效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate expiryDate;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

}
