package com.iboot.pf.module.mes.service.materialreq.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领退补料确认参数。
 *
 * <p>确认是领料三段的最后一段，把料真正从仓库搬到车间（退料则相反），是唯一改动
 * {@code qty_onhand} 的一段。确认之后单据进终态，同一张单不允许再确认第二次——
 * 幂等键是整单固定的 {@code {来源模块}:{申请单号}:CONFIRM_MATERIAL_REQ:0}。
 *
 * <p>因此"这次只发了一部分"不是"待续"，而是"这张单就发这么多"：单据落
 * {@link com.iboot.pf.module.mes.enums.MesMaterialReqStatusEnum#PARTIAL}，
 * 剩余量要另开补料单。这与车间实际一致：仓库这次拿不出全部，剩下的什么时候有货是另一回事，
 * 挂在原单上等于让单据长期停在中间态，谁都不知道该不该开工。
 *
 * <p>{@link #lines} 留空表示全部行按剩余量全额确认，这是最常见的一次性发料。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesConfirmMaterialReqBO extends MesCommandReqDTO {

    /** 领退补料申请编号，必填。 */
    private Long materialReqId;

    /** 确认备注，写进申请备注与 WMS 单据备注。 */
    private String remark;

    /**
     * 逐行确认参数，留空表示全部行按剩余量全额确认。
     *
     * <p>只给部分行时，未给的行按 0 处理（本次不发），单据落 {@code PARTIAL}。
     */
    private List<Line> lines;

    /** 逐行确认参数。 */
    @Data
    public static class Line {

        /** 申请明细行号，与 {@code mes_material_req_line.line_no} 对应，必填。 */
        private Integer lineNo;

        /** 本次确认数量，留空表示按剩余量（申请量 − 已处理量）全额确认。 */
        private BigDecimal confirmQty;

        /**
         * 库位编号：领料与补料是出库来源库位，退料是入库目标库位。
         *
         * <p>留空时领料与补料回落到预留上记的库位；退料没有预留可依据，必须显式给。
         */
        private Long locId;

    }

}
