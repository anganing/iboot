package com.iboot.pf.module.mes.service.kitsnapshot.bo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 一条物料需求，齐套检查与领料申请共用的展开结果。
 *
 * <p>由 {@link com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander} 产出：
 * 虚拟件已经向下展开成子件、倒冲件已经剔除、损耗率已经按公式摊进 {@link #reqQty}，
 * 同一物料在多道工序出现时已合并成一行。
 *
 * <p>齐套与领料共用一套展开结果不是省事，是必须：两处各算一遍需求量，齐套判齐了而领料少领一行，
 * 车间照样开不了工，而且没人能说清哪个数是对的。
 */
@Data
public class MesKitRequirementBO {

    /** 需求物料编号。同一次展开内唯一。 */
    private Long matId;

    /** 需求数量，已按 {@code 父项数量 × 单位用量 ÷ (1 - 损耗率)} 算出，精度 decimal(20,6)。 */
    private BigDecimal reqQty;

    /**
     * 路线工序用料编号，0 表示不是工序用料。
     *
     * <p>整单 BOM 展开时恒为 0；工序用料展开且同一物料出现在多道工序时取第一次出现的那条，
     * 只用于领料行回指主数据，不参与任何计算。
     */
    private Long routeOpMatId;

    /** 计量单位编号，来自 BOM 行或工序用料行。 */
    private Long uomId;

    /**
     * 是否必须开工前发料。
     *
     * <p>合并同物料时按"任一为真则为真"取值：同一物料在 A 工序可以后补、在 B 工序必须先发，
     * 合并后必须服从更严的那条，否则 B 工序会被放行。
     */
    private Boolean mustIssueBeforeStart;

}
