package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 生产工单关闭结果。
 *
 * <p>只有一项内容，但这一项不能丢：<b>发下去没用完、也还没退回仓库的物料清单</b>。
 * 关闭不自动退料（车间关单时料可能还在工位上，自动退料等于把还在用的料从账上收走），
 * 所以界面必须把这份清单摆给车间看 —— 关闭之后账已封版，谁手上还剩什么料就只能靠它。
 * 端点把返回值丢掉等于把这个功能做废。
 *
 * <p>退不退、退多少由人看着现场决定，走 {@code RETURN} 类型的领退补料申请。
 */
@Schema(description = "管理后台 - MES-生产工单关闭 Response VO")
@Data
public class ProdOrderCloseRespVO {

    @Schema(description = "未退回的多余领料清单，全部用完时为空表")
    private List<UnreturnedLine> unreturnedLines;

    /** 一个物料的多余量。 */
    @Schema(description = "多余领料明细")
    @Data
    public static class UnreturnedLine {

        @Schema(description = "物料编号", example = "1")
        private Long matId;

        @Schema(description = "物料编码", example = "MAT-1001")
        private String matCode;

        @Schema(description = "物料名称")
        private String matName;

        @Schema(description = "净发料量：领料与补料的已处理量之和减去退料的已处理量", example = "90")
        private BigDecimal issuedQty;

        @Schema(description = "按实际产量算出的应耗量", example = "60")
        private BigDecimal requiredQty;

        @Schema(description = "多余量 = 净发料量 − 应耗量，恒大于零", example = "30")
        private BigDecimal overQty;

    }

}
