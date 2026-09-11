package com.iboot.pf.module.mes.api.quality.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 创建质检任务命令。
 *
 * <p>不带检验方案编号与版本：方案由规则按触发点、物料、供应商等条件解析后锁定，
 * 调用方指定方案就绕过了规则，事后无法解释"这批为什么按这个方案检"。
 */
@Schema(description = "RPC 服务 - 创建质检任务 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateQcTaskReqDTO extends MesCommandReqDTO {

    @Schema(description = "检验对象类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MATERIAL")
    private String objectType;

    /** 触发点，取值为 MesQcTriggerPointEnum 的枚举名。 */
    @Schema(description = "触发点", requiredMode = Schema.RequiredMode.REQUIRED, example = "IQC")
    private String triggerPoint;

    /** 来源业务类型，取值为 MesBizTypeEnum 的枚举名。 */
    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ORDER")
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED, example = "PO202609040001")
    private String bizNo;

    @Schema(description = "来源业务明细编号，0 表示无明细", example = "1")
    private Long bizLineId;

    @Schema(description = "检验物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验物料编号不能为空")
    private Long matId;

    private Long matCatId;
    private Long opId;
    private Long wcId;
    private Long supplierId;

    @Schema(description = "检验批次编号，0 表示不区分批次", example = "1")
    private Long batchId;

    @Schema(description = "送检数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "送检数量不能为空")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "指派检验员编号，0 表示未指派", example = "1")
    private Long assignedInspectorId;

    @Schema(description = "备注")
    private String remark;

}
