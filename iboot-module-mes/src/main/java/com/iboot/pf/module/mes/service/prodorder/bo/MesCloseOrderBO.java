package com.iboot.pf.module.mes.service.prodorder.bo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 关闭生产工单参数。
 *
 * <p>关闭是账目收尾动作，除了状态推进还要做四件事：释放剩余预留、倒冲件反冲扣料、回写需求状态、写在制流水。
 * 前两件会真的动库存，因此比其他状态动作多两个仓储维度。
 *
 * <p>{@code whId} / {@code locId} 只服务倒冲反冲那一步：倒冲件从来不预留（齐套与领料都把它剔除），
 * 没有"预留占住的库位"可以回落，所以要么调用方指定，要么回落到倒冲物料自己的默认仓库与默认库位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesCloseOrderBO extends MesOrderActionBO {

    /**
     * 倒冲反冲的发料仓库，留空回落倒冲物料的默认仓库。
     *
     * <p>出库单头上只有一个仓库，因此各倒冲物料的默认仓库必须一致；不一致又没给这个入参时报错，
     * 而不是随便挑一个——挑错仓库等于从别人的库存里扣料。
     */
    private Long whId;

    /** 倒冲反冲的发料库位，留空回落各倒冲物料自己的默认库位。 */
    private Long locId;

}
