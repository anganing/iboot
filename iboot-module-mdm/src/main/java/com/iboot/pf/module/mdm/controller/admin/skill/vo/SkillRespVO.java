package com.iboot.pf.module.mdm.controller.admin.skill.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 技能 Response VO")
@Data
@ExcelIgnoreUnannotated
public class SkillRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "技能编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("技能编码")
    private String code;

    @Schema(description = "技能名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "焊接")
    @ExcelProperty("技能名称")
    private String name;

    @Schema(description = "等级体系说明")
    @ExcelProperty("等级体系说明")
    private String levelDesc;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
