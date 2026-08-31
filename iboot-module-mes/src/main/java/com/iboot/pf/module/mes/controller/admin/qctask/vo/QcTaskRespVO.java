package com.iboot.pf.module.mes.controller.admin.qctask.vo;

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
 * 质检任务 Response VO。
 *
 * <p>规则与方案的编码名称来自模块内 JOIN；检验员昵称由 Service 调 system 门面批量补全。
 */
@Schema(description = "管理后台 - MES-质检任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class QcTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "质检任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("质检任务号")
    private String taskNo;

    @Schema(description = "命中的质检规则编号", example = "1")
    private Long ruleId;

    @Schema(description = "质检规则编码")
    @ExcelProperty("质检规则编码")
    private String ruleCode;

    @Schema(description = "质检规则名称")
    @ExcelProperty("质检规则名称")
    private String ruleName;

    @Schema(description = "锁定的检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long qcPlanId;

    @Schema(description = "检验方案编码")
    @ExcelProperty("检验方案编码")
    private String qcPlanCode;

    @Schema(description = "检验方案名称")
    @ExcelProperty("检验方案名称")
    private String qcPlanName;

    @Schema(description = "锁定的检验方案版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    @ExcelProperty("方案版本")
    private String qcPlanVersion;

    @Schema(description = "触发点", requiredMode = Schema.RequiredMode.REQUIRED, example = "IPQC")
    @ExcelProperty(value = "触发点", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_TRIGGER_POINT)
    private String triggerPoint;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "OP_TASK")
    @ExcelProperty(value = "来源业务类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.BIZ_TYPE)
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务明细编号", example = "1")
    private Long bizLineId;

    @Schema(description = "检验物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料名称")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("规格型号")
    private String matSpec;

    @Schema(description = "检验批次编号", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    @ExcelProperty("批次号")
    private String lotNo;

    @Schema(description = "送检数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @ExcelProperty("送检数量")
    private BigDecimal qty;

    @Schema(description = "抽样数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @ExcelProperty("抽样数量")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位")
    private String uomCode;

    @Schema(description = "指派检验员编号", example = "1")
    private Long assignedInspectorId;

    @Schema(description = "指派检验员")
    @ExcelProperty("检验员")
    private String assignedInspectorName;

    @Schema(description = "检验开始时间")
    @ExcelProperty("检验开始时间")
    private LocalDateTime inspectStartTime;

    @Schema(description = "检验结束时间")
    @ExcelProperty("检验结束时间")
    private LocalDateTime inspectEndTime;

    @Schema(description = "检验结论", example = "QUALIFIED")
    @ExcelProperty(value = "检验结论", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_CONCLUSION)
    private String conclusion;

    @Schema(description = "合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "CREATED")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.QC_TASK_STATUS)
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
