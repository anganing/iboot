package com.iboot.pf.module.mes.api.demand.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 生产需求跨模块只读事实。 */
@Schema(description = "RPC 服务 - 生产需求 Response DTO")
@Data
public class ProdDemandRespDTO {

    @Schema(description = "生产需求编号", example = "1")
    private Long id;

    @Schema(description = "生产需求单号", example = "PD202609040001")
    private String demandNo;

    /** 来源类型，取值为 MesDemandSourceTypeEnum 的枚举名。 */
    @Schema(description = "来源类型", example = "SALE_ORDER")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", example = "M-0001")
    private String matCode;

    @Schema(description = "物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "需求数量", example = "100")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", example = "PCS")
    private String uomCode;

    @Schema(description = "需求交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", example = "10")
    private Integer priority;

    /** 需求状态，取值为 MesDemandStatusEnum 的枚举名。 */
    @Schema(description = "需求状态", example = "CONFIRMED")
    private String status;

}
