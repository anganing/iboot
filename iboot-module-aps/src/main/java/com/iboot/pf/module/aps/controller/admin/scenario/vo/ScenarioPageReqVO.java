package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 排程场景分页 Request VO")
@Data
public class ScenarioPageReqVO extends PageParam {

    @Schema(description = "排程场景编码")
    private String code;

    @Schema(description = "排程场景名称", example = "张三")
    private String name;

    @Schema(description = "工厂编号", example = "20450")
    private Long plantId;

    @Schema(description = "车间编号，0表示全工厂", example = "14740")
    private Long workshopId;

    @Schema(description = "排程策略编号", example = "18129")
    private Long strategyId;

    @Schema(description = "计划窗口开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planStartTime;

    @Schema(description = "计划窗口结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] planEndTime;

    @Schema(description = "输入数据快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

    @Schema(description = "状态：DRAFT、SNAPSHOTTING、READY、SOLVING、SOLVED、CLOSED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}