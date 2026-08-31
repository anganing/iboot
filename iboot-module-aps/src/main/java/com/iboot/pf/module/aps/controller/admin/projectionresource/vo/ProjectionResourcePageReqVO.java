package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;
import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 资源投影分页 Request VO。 */
@Schema(description = "管理后台 - APS-资源投影分页 Request VO")
@Data
public class ProjectionResourcePageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "MDM 生产资源编号", example = "1")
    private Long sourceResId;

    @Schema(description = "资源编码")
    private String resCode;

    @Schema(description = "资源名称")
    private String resName;

    @Schema(description = "资源类型", example = "EQUIPMENT")
    private String resType;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "工作中心编号", example = "1")
    private Long wcId;

    @Schema(description = "班次编号，0 表示日汇总", example = "1")
    private Long shiftId;

    @Schema(description = "产能日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate[] workDate;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

}
