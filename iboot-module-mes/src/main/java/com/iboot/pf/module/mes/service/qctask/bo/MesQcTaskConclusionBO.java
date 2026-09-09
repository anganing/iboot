package com.iboot.pf.module.mes.service.qctask.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 质检任务提交结论参数。
 *
 * <p>与工序域 {@code MesOpTaskFinishBO extends MesOpTaskActionBO} 同构：结论是四个动作里唯一带
 * 数量语义的，单独一个载体，避免把四个动作共用的 {@link MesQcTaskActionBO} 摊宽。
 *
 * <p>{@code qtyQualified + qtyRejected} 必须等于任务抽检数 {@code sample_qty}
 * （见 {@code MesBusinessRules.validateQualityQty}）：抽检了多少件，判定的口径必须把每一件的去向说清，
 * 合格与不合格对不上抽样数说明有数量被漏判或重复判。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesQcTaskConclusionBO extends MesQcTaskActionBO {

    /** 检验结论，取值见 MesQcConclusionEnum：QUALIFIED / REJECTED / CONCESSION，必填。 */
    private String conclusion;

    /** 合格数量，必填。让步接收（CONCESSION）按合格数落库，处置依据记在缺陷与处置单上。 */
    private BigDecimal qtyQualified;

    /** 不合格数量，必填。 */
    private BigDecimal qtyRejected;

}
