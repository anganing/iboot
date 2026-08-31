package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 排程场景分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - APS-排程场景分页 Request VO")
@Data
public class ScenarioPageReqVO extends PageParam {

    @Schema(description = "排程场景编码")
    private String code;

    @Schema(description = "排程场景名称")
    private String name;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "排程策略编号", example = "1")
    private Long strategyId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "计划窗口开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planStartTime;

    @Schema(description = "计划窗口结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planEndTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
