package com.iboot.pf.module.mes.api.materialreq.dto;

import com.iboot.pf.module.mes.api.dto.MesCommandReqDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建领退补料申请命令。
 *
 * <p>{@code lines} 留空时由服务端按用料关系展开：给了 {@code opTaskId} 取该工序的路线用料，
 * 只给 {@code prodOrderId} 取整单 BOM。退料这类没有用料关系可推的场景才需要调用方显式给行。
 */
@Schema(description = "RPC 服务 - 创建领退补料申请 Request DTO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CreateMaterialReqReqDTO extends MesCommandReqDTO {

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "作业工单编号，0 表示工单级申请", example = "1")
    private Long workOrderId;

    @Schema(description = "工序任务编号，0 表示非工序级申请", example = "1")
    private Long opTaskId;

    /** 申请类型，取值为 MesMaterialReqTypeEnum 的枚举名。 */
    @Schema(description = "申请类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ISSUE")
    private String reqType;

    @Schema(description = "库存组织编号", example = "1")
    private Long invOrgId;

    @Schema(description = "目标仓库或来源仓库编号", example = "1")
    private Long whId;

    @Schema(description = "申请明细行，为空时由服务端按用料关系展开")
    @Valid
    private List<Line> lines;

    @Schema(description = "备注")
    private String remark;

    /** 创建领退补料申请的明细行。 */
    @Schema(description = "RPC 服务 - 创建领退补料申请明细 Request DTO")
    @Data
    public static class Line {

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "申请数量不能为空")
        private BigDecimal qty;

        @Schema(description = "计量单位编号", example = "1")
        private Long uomId;

        @Schema(description = "指定批次编号，0 表示不指定批次", example = "1")
        private Long batchId;

        @Schema(description = "路线工序用料编号，0 表示非 BOM 用料", example = "1")
        private Long routeOpMatId;

        @Schema(description = "备注")
        private String remark;

    }

}
