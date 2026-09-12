package com.iboot.pf.module.mes.controller.admin.trace.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesTraceDirectionEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次追溯导出 Request VO。
 *
 * <p>追溯查询是 {@code /forward}、{@code /backward} 两个端点，导出只给一个 {@code /export-excel}
 * （客户审核交材料要的是同一张表），因此方向退化为入参，由 {@link MesTraceDirectionEnum} 决定走哪个解析器。
 * 起点类型与起点值和两个查询端点完全一致，导出结果就是页面当前看到的那棵树。
 */
@Schema(description = "管理后台 - 批次追溯导出 Request VO")
@Data
public class TraceExportReqVO {

    /** 追溯方向：FORWARD 正向 / BACKWARD 反向。 */
    @Schema(description = "追溯方向：FORWARD、BACKWARD", requiredMode = Schema.RequiredMode.REQUIRED, example = "FORWARD")
    @NotNull(message = "追溯方向不能为空")
    @InEnum(MesTraceDirectionEnum.class)
    private String direction;

    /** 起点类型：LOT / PROD_ORDER / SALES_OUT。 */
    @Schema(description = "起点类型：LOT、PROD_ORDER、SALES_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT")
    @NotNull(message = "起点类型不能为空")
    @InEnum(MesTraceStartTypeEnum.class)
    private String startType;

    /** 起点值：批次号 / 生产工单号 / 销售出库单号。 */
    @Schema(description = "起点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT20260801")
    @NotBlank(message = "起点值不能为空")
    private String startValue;

    /** 转查询 BO（方向不在 BO 里，Service 由 direction 自行分发）。 */
    public MesTraceQueryBO toQueryBO() {
        MesTraceQueryBO queryBO = new MesTraceQueryBO();
        queryBO.setStartType(startType);
        queryBO.setStartValue(startValue);
        return queryBO;
    }

}
