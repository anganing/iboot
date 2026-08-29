package com.iboot.pf.module.mdm.controller.admin.routeopmat.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 工序用料 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RouteOpMatRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30511")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30682")
    @ExcelProperty("路线工序编号")
    private Long routeOpId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "11509")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "规格型号")
    private String matSpec;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数量")
    private BigDecimal qty;

    @Schema(description = "损耗率")
    @ExcelProperty("损耗率")
    private BigDecimal scrapRate;

    @Schema(description = "供料方式：PICK-领料，BACKFLUSH-倒冲，PHANTOM-虚拟件，DIRECT-直送线边")
    @ExcelProperty("供料方式")
    private String issueMethod;

    @Schema(description = "是否必须开工前发料")
    @ExcelProperty("是否必须开工前发料")
    private Boolean mustIssueBeforeStart;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25404")
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码")
    private String uomCode;

    @Schema(description = "计量单位名称")
    private String uomName;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
