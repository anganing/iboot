package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工单投影 Response VO。
 *
 * <p>数量、交期、状态都是刷新时从 mes 取的快照；场景由 Mapper JOIN 取出，
 * 工厂、车间、项目、资源、物料名称由 Service 走门面批量补全。
 */
@Schema(description = "管理后台 - APS-工单投影 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProjectionOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long scenarioId;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "OP_TASK")
    @ExcelProperty(value = "来源类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.PROJECTION_SOURCE_TYPE)
    private String sourceType;

    @Schema(description = "MES 来源对象编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long sourceId;

    @Schema(description = "MES 来源对象单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源单号")
    private String sourceNo;

    @Schema(description = "生产工单编号，0 表示尚未生成工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示尚未生成作业工单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序投影", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "车间编号，0 表示未指定", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
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

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "路线工序编号，0 表示需求或整单", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeOpId;

    @Schema(description = "当前资源编号，0 表示未分配", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long resId;

    @Schema(description = "当前资源名称")
    @ExcelProperty("当前资源")
    private String resName;

    @Schema(description = "计划数量或剩余数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("计划数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "来源对象状态快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "来源状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.OP_TASK_STATUS)
    private String currentStatus;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "是否已开工或业务锁定", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "是否锁定", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.BOOLEAN_STRING)
    private Boolean lockedFlag;

    @Schema(description = "来源数据更新时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @Schema(description = "投影快照时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
