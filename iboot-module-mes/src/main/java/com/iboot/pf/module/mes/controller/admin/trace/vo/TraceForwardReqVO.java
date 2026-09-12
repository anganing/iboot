package com.iboot.pf.module.mes.controller.admin.trace.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesTraceStartTypeEnum;
import com.iboot.pf.module.mes.service.trace.bo.MesTraceQueryBO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 批次正向追溯请求 VO。
 *
 * <p>任务书要求 Controller 走 GET + 权限码 {@code mes:trace:query}；VO 与 Service 层 BO 分开，
 * 由 {@link #toQueryBO()} 做转换，前端契约变化不渗到 Service。
 */
@Schema(description = "管理后台 - 批次正向追溯 Request VO")
@Data
public class TraceForwardReqVO {

    /** 起点类型：LOT / PROD_ORDER / SALES_OUT。 */
    @Schema(description = "起点类型：LOT、PROD_ORDER、SALES_OUT", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOT")
    @NotNull(message = "起点类型不能为空")
    @InEnum(MesTraceStartTypeEnum.class)
    private String startType;

    /** 起点值：批次号 / 生产工单号 / 销售出库单号。 */
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
