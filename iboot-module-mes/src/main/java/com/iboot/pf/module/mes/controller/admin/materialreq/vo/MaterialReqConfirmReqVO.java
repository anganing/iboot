package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 领退补料申请确认请求：把料真正从仓库搬到车间（退料则相反）。
 *
 * <p>这是领料三段里唯一改动在库量的一段，也是一次性动作 —— 幂等键整单固定，
 * 重复确认直接命中幂等不产生第二笔过账。因此"这次只发了一部分"不是"待续"而是"这张单就发这么多"：
 * 单据落部分完成，剩余量另开补料单。
 *
 * <p>{@link #lines} 留空表示全部行按剩余量全额确认，这是最常见的一次性发料。
 *
 * <p><b>逐行给库位是这个请求存在的关键</b>（承接 T1-11 遗留 2）：提交预留时没有把库位定下来，
 * 生产环境的预留行库位为空，确认时回落取不到，只能由这里逐行指定。库位解析顺序是"入参优先、
 * 回落到预留记的库位"，两者都没有时服务端抛错并带出物料编码。为什么不让 MES 自己挑一个有货的库位：
 * 预留占的是哪条库存记录，出库就必须从那条记录出，从别处拿会让两个库位的账都错；挑库位是 WMS 拣选策略的职责。
 */
@Schema(description = "管理后台 - MES-领退补料申请确认 Request VO")
@Data
public class MaterialReqConfirmReqVO {

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领退补料申请编号不能为空")
    private Long id;

    @Schema(description = "幂等键，留空由服务端按申请单号与动作派生")
    @Size(max = 128, message = "幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "确认备注，写进申请备注与 WMS 单据备注")
    @Size(max = 500, message = "确认备注长度不能超过 500")
    private String remark;

    @Schema(description = "逐行确认参数，留空表示全部行按剩余量全额确认；只给部分行时未给的行本次不发")
    @Valid
    private List<Line> lines;

    /** 逐行确认参数。 */
    @Schema(description = "领退补料确认明细")
    @Data
    public static class Line {

        @Schema(description = "申请明细行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "申请明细行号不能为空")
        private Integer lineNo;

        @Schema(description = "本次确认数量，留空表示按剩余量（申请量 − 已处理量）全额确认", example = "100")
        @DecimalMin(value = "0", message = "本次确认数量不能为负数")
        private BigDecimal confirmQty;

        @Schema(description = "库位编号：领料与补料是出库来源库位，退料是入库目标库位；"
                + "领料留空时回落预留上记的库位，退料必须给", example = "1")
        private Long locId;

    }

}
