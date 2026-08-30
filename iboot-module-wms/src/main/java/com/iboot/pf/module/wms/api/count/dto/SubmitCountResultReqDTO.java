package com.iboot.pf.module.wms.api.count.dto;

import com.iboot.pf.module.wms.api.dto.WmsCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/** 提交实盘结果命令 DTO。 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SubmitCountResultReqDTO extends WmsCommandReqDTO {

    /** 盘点单编号。 */
    @Schema(description = "盘点单编号")
    private Long orderId;

    /** 盘点单号，与 orderId 二选一。 */
    @Schema(description = "盘点单号")
    private String countNo;

    /** 实盘结果。 */
    @Schema(description = "实盘结果")
    @Valid
    @NotEmpty(message = "实盘结果不能为空")
    private List<LineResult> results;

    /** 实盘结果。 */
    @Data
    public static class LineResult {

        /** 盘点明细编号。 */
        @Schema(description = "盘点明细编号")
        private Long lineId;

        /** 行号，与 lineId 二选一。 */
        @Schema(description = "行号")
        private Integer lineNo;

        /** 实盘数量。 */
        @Schema(description = "实盘数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.000000")
        @NotNull(message = "实盘数量不能为空")
        private BigDecimal actualQty;

        /** 备注。 */
        @Schema(description = "备注")
        private String remark;

    }

}
