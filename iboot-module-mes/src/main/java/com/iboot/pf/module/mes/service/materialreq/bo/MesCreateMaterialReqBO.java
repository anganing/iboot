package com.iboot.pf.module.mes.service.materialreq.bo;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建领退补料申请参数。
 *
 * <p>{@link #lines} 留空时由服务端按用料关系展开：给了 {@code opTaskId} 取该工序的路线用料，
 * 只给 {@code prodOrderId} 取整单 BOM。展开走
 * {@link com.iboot.pf.module.mes.service.kitsnapshot.MesKitRequirementExpander}，
 * 与齐套检查同一套需求量口径——两处各算一遍的话，齐套判齐了而领料少领一行，车间照样开不了工。
 *
 * <p>退料没有用料关系可推（退多少是车间数出来的），必须显式给行，且每行必须指定批次：
 * 退回去的库存要落到原批次上，否则批次台账对不上。
 *
 * <p>创建有落库载体（{@code mes_material_req.idempotency_key}），因此走列判重而不是命令记录表。
 * 幂等键留空时按 {@code {来源模块}:{工单号}:CREATE_REQ:0} 派生。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class MesCreateMaterialReqBO extends MesCommandReqDTO {

    /** 生产工单编号，必填。领退补料一律挂在生产工单下。 */
    private Long prodOrderId;

    /** 作业工单编号，0 表示工单级申请。 */
    private Long workOrderId;

    /** 工序任务编号，0 表示非工序级申请。给了它就按该工序的用料展开明细。 */
    private Long opTaskId;

    /** 申请类型，必填，取 {@link com.iboot.pf.module.mes.enums.MesMaterialReqTypeEnum} 的枚举名。 */
    private String reqType;

    /** 库存组织编号，必填。预留与出入库都要落到具体库存组织上。 */
    private Long invOrgId;

    /** 领料的来源仓库或退料的目标仓库编号，必填。 */
    private Long whId;

    /** 申请明细行，留空时由服务端按用料关系展开。 */
    private List<Line> lines;

    /** 备注。 */
    private String remark;

    /** 申请明细行。 */
    @Data
    public static class Line {

        /** 物料编号，必填。 */
        private Long matId;

        /** 申请数量，必填。 */
        private BigDecimal qty;

        /** 计量单位编号，为空时取物料主档的基本单位。 */
        private Long uomId;

        /** 指定批次编号，0 表示不指定批次；退料必填。 */
        private Long batchId;

        /** 路线工序用料编号，0 表示不是工序用料。 */
        private Long routeOpMatId;

        /** 是否必须开工前发料，为空时按 {@code true} 处理。 */
        private Boolean mustIssueBeforeStart;

        /** 备注。 */
        private String remark;

    }

}
