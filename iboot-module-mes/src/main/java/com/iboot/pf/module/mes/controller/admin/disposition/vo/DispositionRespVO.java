package com.iboot.pf.module.mes.controller.admin.disposition.vo;

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
 * 不合格品处置单 Response VO。
 *
 * <p>质检任务信息来自模块内 JOIN；审批人昵称由 Service 调 system 门面批量补全。
 */
@Schema(description = "管理后台 - MES-不合格品处置单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DispositionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "不合格处置单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("处置单号")
    private String dispositionNo;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "质检任务号")
    @ExcelProperty("质检任务号")
    private String taskNo;

    @Schema(description = "检验物料编码")
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "检验物料名称")
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "处置类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "REWORK")
    @ExcelProperty(value = "处置类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DISPOSITION_TYPE)
    private String dispositionType;

    @Schema(description = "处置数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("处置数量")
    private BigDecimal qty;

    @Schema(description = "联动目标业务类型", example = "REWORK")
    @ExcelProperty(value = "联动目标业务类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BIZ_TYPE)
    private String targetBizType;

    @Schema(description = "联动目标业务单号")
    @ExcelProperty("联动目标业务单号")
    private String targetBizNo;

    @Schema(description = "审批人员编号", example = "1")
    private Long approvedBy;

    @Schema(description = "审批人员")
    @ExcelProperty("审批人员")
    private String approvedByName;

    @Schema(description = "审批时间")
    @ExcelProperty("审批时间")
    private LocalDateTime approveTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.DISPOSITION_STATUS)
    private String status;

    @Schema(description = "处置幂等键")
    private String idempotencyKey;

    @Schema(description = "处置说明")
    @ExcelProperty("处置说明")
    private String remark;

    @Schema(description = "流程审批状态，取值见 BpmProcessInstanceStatusEnum")
    private Integer approvalStatus;

    @Schema(description = "流程实例编号")
    private String processInstanceId;

    @Schema(description = "审批完成时间")
    private LocalDateTime approvalTime;

    @Schema(description = "审批意见或驳回原因")
    private String approvalReason;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
