package com.iboot.pf.module.mes.api.kit.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 齐套检查命令。
 *
 * <p>不带父项数量：需求量按"工单数量 × 单位用量 ÷ (1 - 损耗率)"算，工单数量由服务端从工单上读，
 * 调用方传进来就等于让它自己决定要检查多少件。
 *
 * <p>{@code checkScope} 决定按哪一层展开需求：{@code PROD_ORDER} 走整单 BOM，
 * {@code WORK_ORDER} 走该段覆盖的路线工序用料，{@code OP_TASK} 走单道工序用料。
 */
@Schema(description = "RPC 服务 - 齐套检查 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class KitCheckReqDTO extends MesCommandReqDTO {

    /** 检查范围，取值为 MesKitCheckScopeEnum 的枚举名。 */
    @Schema(description = "检查范围", requiredMode = Schema.RequiredMode.REQUIRED, example = "PROD_ORDER")
    private String checkScope;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示整单检查", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序检查", example = "1")
    private Long opTaskId;

    @Schema(description = "检查用仓库编号，0 表示按工单默认仓库取可用量", example = "1")
    private Long whId;

}
