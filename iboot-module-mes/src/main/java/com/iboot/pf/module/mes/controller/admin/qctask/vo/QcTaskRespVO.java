package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-质检任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "19592")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("质检任务号")
    private String taskNo;

    @Schema(description = "命中的质检规则编号", example = "1712")
    @ExcelProperty("命中的质检规则编号")
    private Long ruleId;

    @Schema(description = "锁定的检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "24589")
    @ExcelProperty("锁定的检验方案编号")
    private Long qcPlanId;

    @Schema(description = "锁定的检验方案版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定的检验方案版本")
    private String qcPlanVersion;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("来源业务类型")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务明细编号", example = "8115")
    @ExcelProperty("来源业务明细编号")
    private Long bizLineId;

    @Schema(description = "检验物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20917")
    @ExcelProperty("检验物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("物料规格快照")
    private String matSpec;

    @Schema(description = "检验批次编号", example = "10315")
    @ExcelProperty("检验批次编号")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号快照")
    private String lotNo;

    @Schema(description = "送检数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("送检数量")
    private BigDecimal qty;

    @Schema(description = "抽样数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("抽样数量")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "28182")
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @Schema(description = "指派检验员编号", example = "1584")
    @ExcelProperty("指派检验员编号")
    private Long assignedInspectorId;

    @Schema(description = "检验开始时间")
    @ExcelProperty("检验开始时间")
    private LocalDateTime inspectStartTime;

    @Schema(description = "检验结束时间")
    @ExcelProperty("检验结束时间")
    private LocalDateTime inspectEndTime;

    @Schema(description = "检验结论：QUALIFIED、REJECTED、CONCESSION")
    @ExcelProperty("检验结论：QUALIFIED、REJECTED、CONCESSION")
    private String conclusion;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED")
    private String status;

    @Schema(description = "创建幂等键")
    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "随便")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "命中的质检规则编码")
    @ExcelProperty("命中的质检规则编码")
    private String ruleCode;

    @Schema(description = "命中的质检规则名称")
    @ExcelProperty("命中的质检规则名称")
    private String ruleName;

    @Schema(description = "锁定的检验方案编码")
    @ExcelProperty("锁定的检验方案编码")
    private String qcPlanCode;

    @Schema(description = "锁定的检验方案名称")
    @ExcelProperty("锁定的检验方案名称")
    private String qcPlanName;

}