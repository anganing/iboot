package com.iboot.pf.module.mes.api.demand.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建生产需求命令。
 *
 * <p>不带状态：新建需求固定落 {@code DRAFT}，确认与取消都是独立动作。
 */
@Schema(description = "RPC 服务 - 创建生产需求 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateProdDemandReqDTO extends MesCommandReqDTO {

    /** 来源类型，取值为 MesDemandSourceTypeEnum 的枚举名。 */
    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "SALE_ORDER")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "需求物料编号不能为空")
    private Long matId;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "需求数量不能为空")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "需求交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", example = "10")
    private Integer priority;

    @Schema(description = "备注")
    private String remark;

}
