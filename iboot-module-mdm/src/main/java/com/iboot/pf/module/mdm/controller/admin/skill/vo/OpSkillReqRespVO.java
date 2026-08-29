package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工序技能要求 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpSkillReqRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("标准工序编号")
    private Long opId;

    @Schema(description = "工序编码")
    @ExcelProperty("工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "技能编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("技能编号")
    private Long skillId;

    @Schema(description = "技能编码")
    @ExcelProperty("技能编码")
    private String skillCode;

    @Schema(description = "技能名称")
    @ExcelProperty("技能名称")
    private String skillName;

    @Schema(description = "最低技能等级", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("最低技能等级")
    private Integer minLevel;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
