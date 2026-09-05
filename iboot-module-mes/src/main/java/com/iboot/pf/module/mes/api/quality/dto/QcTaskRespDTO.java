package com.iboot.pf.module.mes.api.quality.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 质检任务跨模块只读事实。 */
@Schema(description = "RPC 服务 - 质检任务 Response DTO")
@Data
public class QcTaskRespDTO {

    @Schema(description = "质检任务编号", example = "1")
    private Long id;

    @Schema(description = "质检任务号", example = "QC202609040001")
    private String taskNo;

    @Schema(description = "命中的质检规则编号，0 表示手工建单", example = "1")
    private Long ruleId;

    @Schema(description = "锁定的检验方案编号", example = "1")
    private Long qcPlanId;

    @Schema(description = "锁定的检验方案版本快照", example = "V1.0")
    private String qcPlanVersion;

    /** 触发点，取值为 MesQcTriggerPointEnum 的枚举名。 */
    @Schema(description = "触发点", example = "IQC")
    private String triggerPoint;

    /** 来源业务类型，取值为 MesBizTypeEnum 的枚举名。 */
    @Schema(description = "来源业务类型", example = "PROD_ORDER")
    private String bizType;

    @Schema(description = "来源业务单号", example = "PO202609040001")
    private String bizNo;

    @Schema(description = "来源业务明细编号，0 表示无明细", example = "1")
    private Long bizLineId;

    @Schema(description = "检验物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", example = "M-0001")
    private String matCode;

    @Schema(description = "物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "检验批次编号，0 表示不区分批次", example = "1")
    private Long batchId;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "送检数量", example = "100")
    private BigDecimal qty;

    @Schema(description = "抽样数量", example = "10")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", example = "PCS")
    private String uomCode;

    @Schema(description = "指派检验员编号，0 表示未指派", example = "1")
    private Long assignedInspectorId;

    @Schema(description = "检验开始时间")
    private LocalDateTime inspectStartTime;

    @Schema(description = "检验结束时间")
    private LocalDateTime inspectEndTime;

    /** 检验结论，取值为 MesQcConclusionEnum 的枚举名。 */
    @Schema(description = "检验结论", example = "QUALIFIED")
    private String conclusion;

    @Schema(description = "合格数量", example = "100")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量", example = "0")
    private BigDecimal qtyRejected;

    /** 任务状态，取值为 MesQcTaskStatusEnum 的枚举名。 */
    @Schema(description = "任务状态", example = "DONE")
    private String status;

}
