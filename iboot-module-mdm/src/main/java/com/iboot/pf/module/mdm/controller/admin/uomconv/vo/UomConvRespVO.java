package com.iboot.pf.module.mdm.controller.admin.uomconv.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 单位换算 Response VO")
@Data
@ExcelIgnoreUnannotated
public class UomConvRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "31484")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9455")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "来源计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7295")
    @ExcelProperty("来源计量单位编号")
    private Long fromUomId;

    @Schema(description = "来源计量单位编码")
    @ExcelProperty("来源计量单位编码")
    private String fromUomCode;

    @Schema(description = "来源计量单位名称")
    @ExcelProperty("来源计量单位名称")
    private String fromUomName;

    @Schema(description = "目标计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "14009")
    @ExcelProperty("目标计量单位编号")
    private Long toUomId;

    @Schema(description = "目标计量单位编码")
    @ExcelProperty("目标计量单位编码")
    private String toUomCode;

    @Schema(description = "目标计量单位名称")
    @ExcelProperty("目标计量单位名称")
    private String toUomName;

    @Schema(description = "换算比例", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("换算比例")
    private BigDecimal ratio;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}