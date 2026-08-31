package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业工单 Response VO。
 *
 * <p>生产工单相关字段来自模块内 JOIN 与快照；工厂、车间、工作中心、项目名称由 Service 调 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-作业工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class WorkOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "作业工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "生产物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "生产物料名称")
    @ExcelProperty("物料名称")
    private String matName;

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

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long wcId;

    @Schema(description = "工作中心名称")
    @ExcelProperty("工作中心")
    private String wcName;

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

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "齐套状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "UNCHECKED")
    @ExcelProperty(value = "齐套状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.KIT_STATUS)
    private String kitStatus;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.WORK_ORDER_STATUS)
    private String status;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
