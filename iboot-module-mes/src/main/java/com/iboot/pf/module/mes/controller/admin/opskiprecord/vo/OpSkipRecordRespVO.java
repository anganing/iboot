package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 工序跳过记录 Response VO。工序任务字段来自模块内 JOIN；人员昵称由 Service 调 system 门面补全。 */
@Schema(description = "管理后台 - MES-工序跳过记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class OpSkipRecordRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "被跳过工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
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

    @Schema(description = "跳过原因类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_APPLICABLE")
    @ExcelProperty(value = "跳过原因类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.SKIP_REASON_TYPE)
    private String skipReasonType;

    @Schema(description = "跳过原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("跳过原因")
    private String skipReason;

    @Schema(description = "操作人员编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long operatorId;

    @Schema(description = "操作人员")
    @ExcelProperty("操作人员")
    private String operatorName;

    @Schema(description = "审批人员编号", example = "1")
    private Long approvedBy;

    @Schema(description = "审批人员")
    @ExcelProperty("审批人员")
    private String approvedByName;

    @Schema(description = "审批状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_REQUIRED")
    @ExcelProperty(value = "审批状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.SKIP_APPROVE_STATUS)
    private String approveStatus;

    @Schema(description = "实际跳过时间")
    @ExcelProperty("跳过时间")
    private LocalDateTime skipTime;

    @Schema(description = "对后续工序、质检及排程的影响说明")
    @ExcelProperty("影响说明")
    private String effectRemark;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.SKIP_RECORD_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
