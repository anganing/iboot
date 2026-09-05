package com.iboot.pf.module.mes.api.quality.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 提交检验结果命令。
 *
 * <p>整批提交检验项结果，任务级结论由服务端按检验项判定汇总，不由调用方直接给：
 * 结论是"所有必检项都合格才合格"的推导结果，调用方给结论会与明细自相矛盾。
 */
@Schema(description = "RPC 服务 - 提交检验结果 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class QcResultSubmitReqDTO extends MesCommandReqDTO {

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "质检任务编号不能为空")
    private Long qcTaskId;

    @Schema(description = "检验员编号，0 表示取当前登录用户", example = "1")
    private Long inspectorId;

    @Schema(description = "检验项结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验项结果不能为空")
    @Valid
    private List<Item> items;

    @Schema(description = "备注")
    private String remark;

    /** 单个检验项的实测结果。 */
    @Schema(description = "RPC 服务 - 检验项结果 Request DTO")
    @Data
    public static class Item {

        @Schema(description = "检验项目编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "检验项目编号不能为空")
        private Long itemId;

        @Schema(description = "样本序号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "样本序号不能为空")
        private Integer sampleNo;

        @Schema(description = "实测值", example = "10.02")
        private String actualValue;

        @Schema(description = "是否合格", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
        @NotNull(message = "是否合格不能为空")
        private Boolean passFlag;

        @Schema(description = "本项判定合格数量", example = "1")
        private BigDecimal qtyQualified;

        @Schema(description = "本项判定不合格数量", example = "0")
        private BigDecimal qtyRejected;

        @Schema(description = "备注")
        private String remark;

    }

}
