package com.iboot.pf.module.mes.service.prodorder.bo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 关闭生产工单的结果。
 *
 * <p>只有一项内容：<b>发下去但没用完、也还没退回仓库的物料清单</b>。
 *
 * <p>为什么不自动退料：车间关单时料可能还在工位上（下一张工单接着用、或者正等着质检判定），
 * 自动生成退料单并过账等于把还在用的料从账上收走，现场与账目当场对不上。所以关闭只把清单摆出来，
 * 退不退、退多少由人看着现场决定，走 {@code RETURN} 类型的领退补料申请。
 */
@Data
public class MesOrderCloseResultBO {

    /** 未退回的多余领料清单，全部用完时为空表。 */
    private List<UnreturnedLine> unreturnedLines;

    /** 一个物料的多余量。 */
    @Data
    public static class UnreturnedLine {

        /** 物料编号。 */
        private Long matId;

        /** 物料编码快照，取自领料明细，不回查 mdm。 */
        private String matCode;

        /** 物料名称快照。 */
        private String matName;

        /** 净发料量：领料与补料的已处理量之和，减去退料的已处理量。 */
        private BigDecimal issuedQty;

        /** 按实际产量算出的应耗量。 */
        private BigDecimal requiredQty;

        /** 多余量 = 净发料量 − 应耗量，恒大于零（不大于零的物料不进清单）。 */
        private BigDecimal overQty;

    }

}
