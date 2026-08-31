package com.iboot.pf.module.aps.controller.admin.task.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程任务 Response VO。
 *
 * <p>版本号、场景编码由 Mapper JOIN 取出；工厂、车间、项目、资源名称由 Service 调 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - APS-排程任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程任务编号")
    private String taskNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "MES 工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeOpId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序编码")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间名称")
    @ExcelProperty("车间")
    private String workshopName;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectId;

    @Schema(description = "项目名称")
    @ExcelProperty("项目")
    private String projectName;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long projectTaskId;

    @Schema(description = "项目任务名称")
    @ExcelProperty("项目任务")
    private String projectTaskName;

    @Schema(description = "排定生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long resId;

    @Schema(description = "资源编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "资源名称")
    @ExcelProperty("资源名称")
    private String resName;

    @Schema(description = "计划开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划开始时间")
    private LocalDateTime startTime;

    @Schema(description = "计划结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划结束时间")
    private LocalDateTime endTime;

    @Schema(description = "计划占用分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "60")
    @ExcelProperty("计划占用分钟数")
    private Integer durationMinutes;

    @Schema(description = "资源内执行顺序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("执行顺序号")
    private Integer sequenceNo;

    @Schema(description = "是否锁定不可移动", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "是否锁定", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.BOOLEAN_STRING)
    private Boolean lockedFlag;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PLANNED")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.TASK_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
