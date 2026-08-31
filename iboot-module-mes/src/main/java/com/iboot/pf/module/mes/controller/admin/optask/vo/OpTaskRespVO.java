package com.iboot.pf.module.mes.controller.admin.optask.vo;

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
 * 工序任务 Response VO。
 *
 * <p>作业工单与生产工单相关字段来自模块内 JOIN；资源名称由 Service 调 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-工序任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "工序任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序任务号")
    private String taskNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "生产工单号")
    @ExcelProperty("生产工单号")
    private String prodOrderNo;

    @Schema(description = "锁定的路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long routeOpId;

    @Schema(description = "标准工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opId;

    @Schema(description = "工序编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序编码")
    private String opCode;

    @Schema(description = "工序名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "工序顺序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("工序顺序")
    private Integer seq;

    @Schema(description = "并行工序组")
    @ExcelProperty("并行组")
    private String parallelGroup;

    @Schema(description = "前序工序任务编号", example = "1")
    private Long prevTaskId;

    @Schema(description = "后序工序任务编号", example = "1")
    private Long nextTaskId;

    @Schema(description = "执行模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "SEQUENTIAL")
    @ExcelProperty(value = "执行模式", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.OP_EXEC_MODE)
    private String execMode;

    @Schema(description = "质检模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
    @ExcelProperty(value = "质检模式", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_MODE)
    private String qcMode;

    @Schema(description = "跳序策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_ALLOWED")
    @ExcelProperty(value = "跳序策略", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.SKIP_POLICY)
    private String skipPolicy;

    @Schema(description = "是否关键工序", requiredMode = Schema.RequiredMode.REQUIRED, example = "false")
    @ExcelProperty(value = "关键工序", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BOOLEAN_STRING)
    private Boolean keyFlag;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @Schema(description = "合格完成数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("合格数量")
    private BigDecimal completedQty;

    @Schema(description = "报废数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "当前分配资源编号", example = "1")
    private Long resId;

    @Schema(description = "资源名称")
    @ExcelProperty("生产资源")
    private String resName;

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

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.OP_TASK_STATUS)
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
