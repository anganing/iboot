package com.iboot.pf.module.mes.api.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/** MES 创建、状态与结果命令 DTO。每个命令都继承公共幂等字段。 */
public final class MesCommandDTOs {
    private MesCommandDTOs() {}

    @Data @EqualsAndHashCode(callSuper = true)
    public static class CreateReq extends MesCommandReqDTO { private Long prodOrderId; private Long matId; private BigDecimal qty; }
    @Data @EqualsAndHashCode(callSuper = true)
    public static class StatusReq extends MesCommandReqDTO { private Long id; private String reason; }
    @Data @EqualsAndHashCode(callSuper = true)
    public static class FinishReq extends StatusReq { private BigDecimal qualifiedQty; private BigDecimal scrapQty; private BigDecimal planQty; }
    @Data @EqualsAndHashCode(callSuper = true)
    public static class QualityResultReq extends MesCommandReqDTO { private Long taskId; private BigDecimal sampleQty; private BigDecimal qualifiedQty; private BigDecimal unqualifiedQty; private String conclusion; }
    @Data @EqualsAndHashCode(callSuper = true)
    public static class KitReq extends MesCommandReqDTO { private Long prodOrderId; private BigDecimal parentQty; }
}
