package com.iboot.pf.module.mdm.controller.admin.tool.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 工序工装 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RouteOpToolRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("路线工序编号")
    private Long routeOpId;

    @Schema(description = "工装编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("工装编号")
    private Long toolId;

    @Schema(description = "工装编码")
    @ExcelProperty("工装编码")
    private String toolCode;

    @Schema(description = "工装名称")
    @ExcelProperty("工装名称")
    private String toolName;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("需求数量")
    private Integer qty;

    @Schema(description = "是否必备")
    @ExcelProperty("是否必备")
    private Boolean requiredFlag;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
