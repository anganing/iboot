package com.iboot.pf.module.mdm.controller.admin.rescapability.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 资源能力 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ResCapabilityRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("生产资源编号")
    private Long resId;

    @Schema(description = "资源编码")
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "资源名称")
    @ExcelProperty("资源名称")
    private String resName;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("标准工序编号")
    private Long opId;

    @Schema(description = "工序编码")
    @ExcelProperty("工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "是否首选资源")
    @ExcelProperty("是否首选资源")
    private Boolean preferredFlag;

    @Schema(description = "专属效率系数")
    @ExcelProperty("专属效率系数")
    private BigDecimal efficiency;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
