package com.iboot.pf.module.mdm.controller.admin.tool.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工装模具刀具 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ToolRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "夹具A")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "工装类型：FIXTURE-工装，MOLD-模具，CUTTER-刀具，GAUGE-量具",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "FIXTURE")
    @ExcelProperty("工装类型")
    private String toolType;

    @Schema(description = "所属工作中心编号")
    private Long wcId;

    @Schema(description = "工作中心编码")
    @ExcelProperty("工作中心编码")
    private String wcCode;

    @Schema(description = "工作中心名称")
    @ExcelProperty("工作中心名称")
    private String wcName;

    @Schema(description = "保有数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("保有数量")
    private Integer qtyTotal;

    @Schema(description = "寿命类型：COUNT-次数，TIME-时长")
    @ExcelProperty("寿命类型")
    private String lifeType;

    @Schema(description = "寿命上限")
    @ExcelProperty("寿命上限")
    private Integer lifeLimit;

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
