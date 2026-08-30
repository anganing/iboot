package com.iboot.pf.module.aps.controller.admin.task.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-工序排程任务分页 Request VO")
@Data
public class TaskPageReqVO extends PageParam {

    @Schema(description = "排程版本编号", example = "31209")
    private Long versionId;

    @Schema(description = "排程任务编号")
    private String taskNo;

    @Schema(description = "生产工单编号", example = "29717")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", example = "27462")
    private Long workOrderId;

    @Schema(description = "MES工序任务编号", example = "21603")
    private Long opTaskId;

    @Schema(description = "路线工序编号", example = "1889")
    private Long routeOpId;

    @Schema(description = "工序编码快照")
    private String opCode;

    @Schema(description = "工序名称快照", example = "张三")
    private String opName;

    @Schema(description = "工厂编号", example = "24208")
    private Long plantId;

    @Schema(description = "车间编号", example = "25008")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", example = "31175")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", example = "5280")
    private Long projectTaskId;

    @Schema(description = "排定生产资源编号", example = "20629")
    private Long resId;

    @Schema(description = "资源编码快照")
    private String resCode;

    @Schema(description = "计划开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "计划结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "计划占用分钟数")
    private Integer durationMinutes;

    @Schema(description = "资源内执行顺序号")
    private Integer sequenceNo;

    @Schema(description = "是否锁定不可移动")
    private Boolean lockedFlag;

    @Schema(description = "状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}