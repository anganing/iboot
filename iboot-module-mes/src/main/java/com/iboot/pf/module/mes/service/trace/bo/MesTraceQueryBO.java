package com.iboot.pf.module.mes.service.trace.bo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次追溯查询参数 BO。
 *
 * <p>起点支持三种（见 {@link MesTraceStartTypeEnum}）：批次号、生产工单号、销售出库单号。
 * 后两种从链路中间切入，查询时双向补全成完整链路。
 */
@Schema(description = "管理后台 - 批次追溯查询参数")
@Data
public class MesTraceQueryBO {

    /** 起点类型。 */
    @Schema(description = "起点类型：LOT、PROD_ORDER、SALES_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT")
    @NotNull(message = "起点类型不能为空")
    @InEnum(MesTraceStartTypeEnum.class)
    private String startType;

    /** 起点值：批次号、生产工单号或销售出库单号。 */
    @Schema(description = "起点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT20260801")
    @NotBlank(message = "起点值不能为空")
    private String startValue;

}
