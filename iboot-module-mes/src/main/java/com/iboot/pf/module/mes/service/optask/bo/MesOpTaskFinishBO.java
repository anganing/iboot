package com.iboot.pf.module.mes.service.optask.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 工序任务完工参数。
 *
 * <p>完工是唯一带数量语义的工序动作，因此单独一个载体。但**它不写数量**：合格数与报废数只能由报工推进
 * （见 {@code MesProgressPropagator}），完工只做两件与数量有关的事——校验累计数量凑齐了、把工序封版。
 * 让完工也能写数量就等于给同一个业务事实开第二条写入路径，分批报工的累计值会被一次完工覆盖掉。
 *
 * <p>{@link #qualifiedQty} 与 {@link #scrapQty} 是**可选的对账声明**：调用方把界面上看到的数量报上来，
 * 服务端要求它与工序任务上的累计值一致，不一致直接拒绝。这道校验防的是界面数据过期——工人看到
 * 100/0 就点了完工，而后台已被另一笔报工改成 90/10，此时完工的是一个他没看见的数量。
 * 两个字段都留空时只校验累计值本身。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesOpTaskFinishBO extends MesOpTaskActionBO {

    /** 调用方声明的累计合格数，可空。给了就必须与工序任务上的 {@code completed_qty} 相等。 */
    private BigDecimal qualifiedQty;

    /** 调用方声明的累计报废数，可空。给了就必须与工序任务上的 {@code scrap_qty} 相等。 */
    private BigDecimal scrapQty;

}
