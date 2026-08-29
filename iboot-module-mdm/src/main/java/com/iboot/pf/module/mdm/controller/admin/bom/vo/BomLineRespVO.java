package com.iboot.pf.module.mdm.controller.admin.bom.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 物料清单明细 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BomLineRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "BOM编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("BOM编号")
    private Long bomId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("行号")
    private Integer lineNo;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码快照")
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("数量")
    private BigDecimal qty;

    @Schema(description = "损耗率")
    @ExcelProperty("损耗率")
    private BigDecimal scrapRate;

    @Schema(description = "供料方式：PICK-领料，BACKFLUSH-倒冲，PHANTOM-虚拟件，DIRECT-直送线边")
    @ExcelProperty("供料方式")
    private String issueMethod;

    @Schema(description = "用料绑定工序顺序号")
    @ExcelProperty("工序顺序号")
    private Integer routeOpSeq;

    @Schema(description = "位号/装配位置")
    @ExcelProperty("位号")
    private String positionNo;

    @Schema(description = "行级生效日期")
    private java.time.LocalDate effectiveDate;

    @Schema(description = "行级失效日期")
    private java.time.LocalDate expiryDate;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码")
    @ExcelProperty("计量单位编码")
    private String uomCode;

    @Schema(description = "计量单位名称")
    @ExcelProperty("计量单位名称")
    private String uomName;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;
}
