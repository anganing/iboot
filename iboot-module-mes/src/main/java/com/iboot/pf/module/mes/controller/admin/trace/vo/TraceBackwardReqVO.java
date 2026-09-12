package com.iboot.pf.module.mes.controller.admin.trace.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次反向追溯请求 VO。
 *
 * <p>字段与正向一致（起点类型 + 起点值），但不与 {@code TraceForwardReqVO} 共用：正向的起点是
 * 原料批次，反向的起点是成品批次，两者是各自端点的契约，将来某一侧要加过滤条件（比如反向只到
 * 二级供应商）不会互相牵连。转换仍走 {@link #toQueryBO()}，Service 层 BO 不感知方向。
 */
@Schema(description = "管理后台 - 批次反向追溯 Request VO")
@Data
public class TraceBackwardReqVO {

    /** 起点类型：LOT（成品批次）/ PROD_ORDER / SALES_OUT。 */
    @Schema(description = "起点类型：LOT、PROD_ORDER、SALES_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT")
    @NotNull(message = "起点类型不能为空")
    @InEnum(MesTraceStartTypeEnum.class)
    private String startType;

    /** 起点值：成品批次号 / 批次编号 / 生产工单号 / 销售出库单号。 */
    @Schema(description = "起点值", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT20260801")
    @NotBlank(message = "起点值不能为空")
    private String startValue;

    /** 转查询 BO。 */
    public MesTraceQueryBO toQueryBO() {
        MesTraceQueryBO queryBO = new MesTraceQueryBO();
        queryBO.setStartType(startType);
        queryBO.setStartValue(startValue);
        return queryBO;
    }

}
