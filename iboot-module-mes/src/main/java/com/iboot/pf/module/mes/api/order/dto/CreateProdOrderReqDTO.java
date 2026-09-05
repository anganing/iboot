package com.iboot.pf.module.mes.api.order.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建生产工单命令。
 *
 * <p>不带 BOM 与工艺路线的四个版本字段：它们在下达动作里按当天的生效版本锁定，调用方传进来会绕过版本锁。
 * 也不带状态：新建工单固定落 {@code DRAFT}，状态只能由动作推进。
 */
@Schema(description = "RPC 服务 - 创建生产工单 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateProdOrderReqDTO extends MesCommandReqDTO {

    @Schema(description = "生产需求编号，0 表示手工建单", example = "1")
    private Long demandId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号，0 表示未指定车间", example = "1")
    private Long workshopId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产物料编号不能为空")
    private Long matId;

    @Schema(description = "计划生产数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划生产数量不能为空")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "工单交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", example = "10")
    private Integer priority;

    @Schema(description = "备注")
    private String remark;

}
