package com.iboot.pf.module.wms.controller.admin.batchmgr.vo;


import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - WMS-批次主档 Response VO")
@Data
@ExcelIgnoreUnannotated
public class BatchRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "23696")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31665")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "李四")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "批次号；无批次物料使用默认批次", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号；无批次物料使用默认批次")
    private String lotNo;

    @Schema(description = "序列号；SN物料以数量为1的批次承载")
    @ExcelProperty("序列号；SN物料以数量为1的批次承载")
    private String snNo;

    @Schema(description = "供应商编号", example = "6991")
    @ExcelProperty("供应商编号")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    @ExcelProperty("供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商批次号")
    @ExcelProperty("供应商批次号")
    private String supplierLotNo;

    @Schema(description = "炉批号")
    @ExcelProperty("炉批号")
    private String furnaceLotNo;

    @Schema(description = "生产日期")
    @ExcelProperty("生产日期")
    private LocalDate produceDate;

    @Schema(description = "失效日期")
    @ExcelProperty("失效日期")
    private LocalDate expiryDate;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：0-启用，1-停用")
    private Integer status;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


}