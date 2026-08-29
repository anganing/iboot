package com.iboot.pf.module.mdm.controller.admin.op.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 标准工序 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "144")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("编码")
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("名称")
    private String name;

    @Schema(description = "标准时长（分钟）", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("标准时长（分钟）")
    private Integer stdDuration;

    @Schema(description = "准备/换型工时（分钟）")
    @ExcelProperty("准备工时（分钟）")
    private Integer setupMinutes;

    @Schema(description = "单件加工工时（分钟）")
    @ExcelProperty("单件加工工时（分钟）")
    private BigDecimal stdMinutesPerUnit;

    @Schema(description = "收尾工时（分钟）")
    @ExcelProperty("收尾工时（分钟）")
    private Integer teardownMinutes;

    @Schema(description = "排队等待时间（分钟）")
    @ExcelProperty("排队等待时间（分钟）")
    private Integer queueMinutes;

    @Schema(description = "转移时间（分钟）")
    @ExcelProperty("转移时间（分钟）")
    private Integer moveMinutes;

    @Schema(description = "时长类型：FIXED-固定工时，PER_UNIT-按件累加", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("时长类型")
    private String durationType;

    @Schema(description = "标准人数")
    @ExcelProperty("标准人数")
    private Integer manQty;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
