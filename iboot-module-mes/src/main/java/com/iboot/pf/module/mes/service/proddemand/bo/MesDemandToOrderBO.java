package com.iboot.pf.module.mes.service.proddemand.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 生产需求转生产工单的动作参数。
 *
 * <p>支持部分转单：一个需求可以拆成多个工单分批投产，每次调用转 {@code qty} 这么多，
 * 累计转满需求数量后需求才置 {@code RELEASED}。已转单量不在需求上冗余存列，而是按
 * {@code mes_prod_order.demand_id} 汇总得出，避免出现"需求上的累计数与工单实际数不一致"的两份真相。
 *
 * <p><b>幂等键必须由调用方给</b>：部分转单场景下同一个需求会被合法地转多次，服务端无法凭需求编号
 * 区分"第二次转单"与"第一次转单的重复提交"。端点或上游命令要为每一次用户操作生成一个键；
 * 留空时服务端按已生成工单数派生一个，只能保证不同批次之间不撞键，拦不住同一批次的重复点击。
 */
@Data
public class MesDemandToOrderBO extends MesCommandReqDTO {

    /** 生产需求编号，必填。 */
    private Long demandId;

    /**
     * 本次转单数量，必填且必须大于零。
     *
     * <p>刻意不做"留空即转剩余全部"：转多少是计划决策，让它显式，避免漏传参数就把整个需求一次性投产。
     */
    private BigDecimal qty;

    /**
     * 车间编号，可选，留空落 0。
     *
     * <p>{@code mes_prod_demand} 上没有车间这一列（需求只到工厂粒度），而 {@code mes_prod_order.workshop_id}
     * 是 NOT NULL，因此这里由调用方补。留空落 0 表示"待指派车间"，下达前应当补齐。
     */
    private Long workshopId;

    /** 工单备注，可为空。 */
    private String remark;

}
