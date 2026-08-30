package com.iboot.pf.module.aps.controller.admin.load.vo;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-资源负荷分页 Request VO")
@Data
public class LoadPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "28723")
    private Long versionId;

    @Schema(description = "生产资源编号", example = "4341")
    private Long resId;

    @Schema(description = "资源编码快照")
    private String resCode;

    @Schema(description = "资源名称快照", example = "王五")
    private String resName;

    @Schema(description = "负荷日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] workDate;

    @Schema(description = "计划负荷分钟数")
    private Integer loadMinutes;

    @Schema(description = "可用产能分钟数")
    private Integer capacityMinutes;

    @Schema(description = "负荷率")
    private BigDecimal loadRate;

    @Schema(description = "超负荷分钟数")
    private Integer overloadMinutes;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}