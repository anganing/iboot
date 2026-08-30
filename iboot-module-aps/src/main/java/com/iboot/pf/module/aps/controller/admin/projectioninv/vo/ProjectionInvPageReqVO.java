package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-WMS库存投影分页 Request VO")
@Data
public class ProjectionInvPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "9799")
    private Long scenarioId;

    @Schema(description = "WMS库存余额编号", example = "29536")
    private Long sourceBalanceId;

    @Schema(description = "库存组织编号", example = "32322")
    private Long invOrgId;

    @Schema(description = "工厂编号", example = "12804")
    private Long plantId;

    @Schema(description = "仓库编号", example = "30832")
    private Long whId;

    @Schema(description = "库位编号", example = "25323")
    private Long locId;

    @Schema(description = "物料编号", example = "31890")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "批次编号", example = "333")
    private Long batchId;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "项目编号，0表示无项目", example = "1600")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "8188")
    private Long projectTaskId;

    @Schema(description = "库存范围：COMMON、PROJECT_DEDICATED")
    private String stockScope;

    @Schema(description = "质量状态", example = "2")
    private String qualityStatus;

    @Schema(description = "现存数量快照")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量快照")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量快照")
    private BigDecimal qtyFrozen;

    @Schema(description = "可用数量快照")
    private BigDecimal qtyAvailable;

    @Schema(description = "来源数据更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sourceUpdateTime;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}