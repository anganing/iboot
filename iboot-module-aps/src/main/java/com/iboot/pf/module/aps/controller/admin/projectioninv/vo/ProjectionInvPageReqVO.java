package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 库存投影分页 Request VO。 */
@Schema(description = "管理后台 - APS-库存投影分页 Request VO")
@Data
public class ProjectionInvPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库位编号", example = "1")
    private Long locId;

    @Schema(description = "批次号")
    private String lotNo;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "库存范围", example = "COMMON")
    private String stockScope;

    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

}
