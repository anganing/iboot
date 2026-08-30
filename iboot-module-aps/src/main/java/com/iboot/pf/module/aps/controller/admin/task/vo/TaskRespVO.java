package com.iboot.pf.module.aps.controller.admin.task.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - 排程任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "15753")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "31209")
    @ExcelProperty("排程版本编号")
    private Long versionId;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程任务编号")
    private String taskNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29717")
    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27462")
    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @Schema(description = "MES工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "21603")
    @ExcelProperty("MES工序任务编号")
    private Long opTaskId;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1889")
    @ExcelProperty("路线工序编号")
    private Long routeOpId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序编码快照")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @ExcelProperty("工序名称快照")
    private String opName;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24208")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "25008")
    @ExcelProperty("车间编号")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "31175")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "5280")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "排定生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20629")
    @ExcelProperty("排定生产资源编号")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码快照")
    private String resCode;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划开始时间")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划结束时间")
    private LocalDateTime endTime;

    @Schema(description = "计划占用分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划占用分钟数")
    private Integer durationMinutes;

    @Schema(description = "资源内执行顺序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源内执行顺序号")
    private Integer sequenceNo;

    @Schema(description = "是否锁定不可移动", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("是否锁定不可移动")
    private Boolean lockedFlag;

    @Schema(description = "状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：PLANNED、LOCKED、RELEASED、STARTED、DONE、CANCELLED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private String versionVersion_no;

}