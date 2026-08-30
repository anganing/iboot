package com.iboot.pf.module.aps.controller.admin.projectionresource.vo;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 资源投影分页 Request VO")
@Data
public class ProjectionResourcePageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "5876")
    private Long scenarioId;

    @Schema(description = "MDM生产资源编号", example = "17785")
    private Long sourceResId;

    @Schema(description = "资源编码快照")
    private String resCode;

    @Schema(description = "资源名称快照", example = "张三")
    private String resName;

    @Schema(description = "资源类型快照", example = "1")
    private String resType;

    @Schema(description = "工厂编号", example = "32372")
    private Long plantId;

    @Schema(description = "车间编号", example = "19808")
    private Long workshopId;

    @Schema(description = "工作中心编号", example = "24830")
    private Long wcId;

    @Schema(description = "产能日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] workDate;

    @Schema(description = "班次编号，0表示日汇总", example = "25903")
    private Long shiftId;

    @Schema(description = "可用分钟数")
    private Integer availableMinutes;

    @Schema(description = "资源能力快照")
    private String capabilityJson;

    @Schema(description = "来源数据更新时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] sourceUpdateTime;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}