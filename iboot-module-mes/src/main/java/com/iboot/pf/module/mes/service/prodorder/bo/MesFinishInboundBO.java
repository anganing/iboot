package com.iboot.pf.module.mes.service.prodorder.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 完工入库参数。
 *
 * <p>支持分批入库：一张工单可以入多次，每次一个幂等键
 * （{@code MES:{工单号}:FINISH_INBOUND:{第几次}}），累计入库量不许超过工单的已完工数量。
 *
 * <p>仓库与库位<b>不由 MES 挑</b>：入参给了用入参，留空时回落到成品物料主档的默认仓库与默认库位
 * （{@code mdm_mat.default_wh_id} / {@code default_loc_id}）。两处都没有就报错——
 * 让 MES 自己找一个"有地方放"的库位等于在 MES 里实现一遍上架策略，那是 WMS 的职责。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesFinishInboundBO extends MesCommandReqDTO {

    /** 生产工单编号，必填。 */
    private Long prodOrderId;

    /**
     * 作业工单编号，可空落 0。
     *
     * <p>只用于入库单头上的 {@code work_order_no} 追溯快照。自动入库时传的是"把整单带到完工的那道
     * 工序所属的作业工单"，也就是末道工序那一段。
     */
    private Long workOrderId;

    /**
     * 本次入库数量，留空表示"已完工数量减去已入库量"的全部剩余。
     *
     * <p>显式给值时仍受累计上限约束：入库量超过已完工数量等于凭空多出成品。
     */
    private BigDecimal qty;

    /** 成品批次号，留空时按 {@code {工单号}-{yyyyMMdd}} 生成。 */
    private String lotNo;

    /** 目标仓库编号，留空回落成品物料的默认仓库。 */
    private Long whId;

    /** 目标库位编号，留空回落成品物料的默认库位。 */
    private Long locId;

    /** 备注。 */
    private String remark;

}
