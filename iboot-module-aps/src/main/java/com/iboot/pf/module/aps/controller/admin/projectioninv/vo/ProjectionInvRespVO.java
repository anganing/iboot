package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - APS-WMS库存投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionInvRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17849")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9799")
    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @Schema(description = "WMS库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29536")
    @ExcelProperty("WMS库存余额编号")
    private Long sourceBalanceId;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32322")
    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12804")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30832")
    @ExcelProperty("仓库编号")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25323")
    @ExcelProperty("库位编号")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31890")
    @ExcelProperty("物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "333")
    @ExcelProperty("批次编号")
    private Long batchId;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("批次号快照")
    private String lotNo;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1600")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "8188")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "库存范围：COMMON、PROJECT_DEDICATED", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("库存范围：COMMON、PROJECT_DEDICATED")
    private String stockScope;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("质量状态")
    private String qualityStatus;

    @Schema(description = "现存数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("现存数量快照")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("预留数量快照")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("冻结数量快照")
    private BigDecimal qtyFrozen;

    @Schema(description = "可用数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("可用数量快照")
    private BigDecimal qtyAvailable;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景名称")
    private String scenarioName;

}