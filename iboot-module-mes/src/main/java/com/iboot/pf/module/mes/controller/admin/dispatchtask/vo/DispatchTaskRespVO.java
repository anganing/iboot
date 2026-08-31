package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 派工任务 Response VO。
 *
 * <p>工序任务相关字段来自模块内 JOIN；人员、班组、资源、班次名称由 Service 调 system 与 mdm 门面批量补全。
 */
@Schema(description = "管理后台 - MES-派工任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DispatchTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "派工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("派工单号")
    private String dispatchNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "工序任务号")
    @ExcelProperty("工序任务号")
    private String opTaskNo;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "作业人员编号", example = "1")
    private Long operatorId;

    @Schema(description = "作业人员")
    @ExcelProperty("作业人员")
    private String operatorName;

    @Schema(description = "作业班组编号", example = "1")
    private Long teamId;

    @Schema(description = "作业班组")
    @ExcelProperty("作业班组")
    private String teamName;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long resId;

    @Schema(description = "生产资源")
    @ExcelProperty("生产资源")
    private String resName;

    @Schema(description = "班次编号", example = "1")
    private Long shiftId;

    @Schema(description = "班次")
    @ExcelProperty("班次")
    private String shiftName;

    @Schema(description = "计划开始时间")
    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DISPATCH_TASK_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
