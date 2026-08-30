package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 排程策略 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StrategyRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "29641")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程策略编码")
    private String code;

    @Schema(description = "排程策略名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("排程策略名称")
    private String name;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("策略版本号")
    private String versionNo;

    @Schema(description = "优化目标说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优化目标说明")
    private String objective;

    @Schema(description = "硬约束、软约束及权重配置", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("硬约束、软约束及权重配置")
    private String configJson;

    @Schema(description = "状态：DRAFT、ENABLED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、ENABLED、DISABLED")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}