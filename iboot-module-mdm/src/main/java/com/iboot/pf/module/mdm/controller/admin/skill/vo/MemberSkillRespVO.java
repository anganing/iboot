package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Schema(description = "管理后台 - 人员技能 Response VO")
@Data
@ExcelIgnoreUnannotated
public class MemberSkillRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "system 用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("用户编号")
    private Long userId;

    @Schema(description = "用户姓名")
    @ExcelProperty("用户姓名")
    private String userName;

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("技能编号")
    private Long skillId;

    @Schema(description = "技能编码")
    @ExcelProperty("技能编码")
    private String skillCode;

    @Schema(description = "技能名称")
    @ExcelProperty("技能名称")
    private String skillName;

    @Schema(description = "技能等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("技能等级")
    private Integer level;

    @Schema(description = "生效日期")
    @ExcelProperty("生效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    @ExcelProperty("失效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate expiryDate;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
