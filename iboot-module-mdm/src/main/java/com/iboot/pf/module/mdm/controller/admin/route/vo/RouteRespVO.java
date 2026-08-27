package com.iboot.pf.module.mdm.controller.admin.route.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 工艺路线 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RouteRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "11381")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25476")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("版本号")
    private String version;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态")
    private Integer status;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生效日期")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    @ExcelProperty("失效日期")
    private LocalDate expiryDate;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
