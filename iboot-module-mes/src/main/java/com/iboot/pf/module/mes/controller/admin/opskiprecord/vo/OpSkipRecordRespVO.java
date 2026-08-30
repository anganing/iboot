package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-工序跳过记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpSkipRecordRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "3690")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "被跳过工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10030")
    @ExcelProperty("被跳过工序任务编号")
    private Long opTaskId;

    @Schema(description = "跳过原因类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("跳过原因类型")
    private String skipReasonType;

    @Schema(description = "跳过原因", requiredMode = Schema.RequiredMode.REQUIRED, example = "不香")
    @ExcelProperty("跳过原因")
    private String skipReason;

    @Schema(description = "操作人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10615")
    @ExcelProperty("操作人员编号")
    private Long operatorId;

    @Schema(description = "审批人员编号")
    @ExcelProperty("审批人员编号")
    private Long approvedBy;

    @Schema(description = "审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED")
    private String approveStatus;

    @Schema(description = "实际跳过时间")
    @ExcelProperty("实际跳过时间")
    private LocalDateTime skipTime;

    @Schema(description = "对后续工序、质检及排程的影响说明", example = "随便")
    @ExcelProperty("对后续工序、质检及排程的影响说明")
    private String effectRemark;

    @Schema(description = "状态：PENDING、EFFECTIVE、REVOKED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("状态：PENDING、EFFECTIVE、REVOKED")
    private String status;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "被跳过工序任务task_no")
    @ExcelProperty("被跳过工序任务task_no")
    private String opTaskTask_no;

}