package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - APS-WMS库存投影新增/修改 Request VO")
@Data
public class ProjectionInvSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17849")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9799")
    @NotNull(message = "排程场景编号不能为空")
    private Long scenarioId;

    @Schema(description = "WMS库存余额编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29536")
    @NotNull(message = "WMS库存余额编号不能为空")
    private Long sourceBalanceId;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32322")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "12804")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30832")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25323")
    @NotNull(message = "库位编号不能为空")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31890")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "批次编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "333")
    @NotNull(message = "批次编号不能为空")
    private Long batchId;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "批次号快照不能为空")
    private String lotNo;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1600")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "8188")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "库存范围：COMMON、PROJECT_DEDICATED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库存范围：COMMON、PROJECT_DEDICATED不能为空")
    private String stockScope;

    @Schema(description = "质量状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "质量状态不能为空")
    private String qualityStatus;

    @Schema(description = "现存数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "现存数量快照不能为空")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预留数量快照不能为空")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "冻结数量快照不能为空")
    private BigDecimal qtyFrozen;

    @Schema(description = "可用数量快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "可用数量快照不能为空")
    private BigDecimal qtyAvailable;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "来源数据更新时间不能为空")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "投影快照时间不能为空")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}