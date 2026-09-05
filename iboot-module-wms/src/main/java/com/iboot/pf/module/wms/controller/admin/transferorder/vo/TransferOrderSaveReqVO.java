package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;
import com.iboot.pf.module.wms.enums.WmsStockScopeEnum;
import com.iboot.pf.module.wms.enums.WmsTransferTypeEnum;

@Schema(description = "管理后台 - WMS-库存转移调拨单新增/修改 Request VO")
@Data
public class TransferOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "转移或调拨单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "转移或调拨单号不能为空")
    private String transferNo;

    @Schema(description = "转移类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "转移类型不能为空")
    @InEnum(WmsTransferTypeEnum.class)
    private String transferType;

    @Schema(description = "来源业务类型")
    @InEnum(WmsSourceModuleEnum.class)
    private String sourceType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库存组织编号不能为空")
    private Long fromInvOrgId;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "453")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long toInvOrgId;

    @Schema(description = "来源仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源仓库编号不能为空")
    private Long fromWhId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long toWhId;

    @Schema(description = "来源项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源项目编号，0表示公共库存不能为空")
    private Long fromProjectId;

    @Schema(description = "目标项目编号，0表示公共库存", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标项目编号，0表示公共库存不能为空")
    private Long toProjectId;

    @Schema(description = "外部设备触发方式", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备触发方式不能为空")
    @InEnum(WmsHookModeEnum.class)
    private String hookMode;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsOrderStatusEnum.class)
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "转移明细")
    @Valid
    private List<Line> lines;

    /**
     * 转移明细。
     *
     * <p>字段名与 {@code WmsCreateTransferOrderBO.Line} 逐一对齐，Controller 直接 {@code BeanUtils.toBean} 转换。
     * 不含 {@code status}：行状态由领域 Service 写死为 DRAFT。
     * 含 {@code matCode}、{@code matName}：转移单领域 Service 不回填物料快照，前端选完物料要一起带过来，
     * 否则库存流水上的物料编码为空。
     */
    @Schema(description = "管理后台 - WMS-库存转移调拨明细")
    @Data
    public static class Line {

        @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "行号不能为空")
        @Min(value = 1, message = "行号必须大于 0")
        private Integer lineNo;

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        @Schema(description = "物料编码快照")
        @Size(max = 64, message = "物料编码长度不能超过 64")
        private String matCode;

        @Schema(description = "物料名称快照")
        @Size(max = 128, message = "物料名称长度不能超过 128")
        private String matName;

        @Schema(description = "批次编号", example = "1")
        private Long batchId;

        @Schema(description = "批次号快照")
        @Size(max = 64, message = "批次号长度不能超过 64")
        private String lotNo;

        @Schema(description = "容器编号，散件留空", example = "1")
        private Long containerId;

        @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "来源库位编号不能为空")
        private Long fromLocId;

        @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "目标库位编号不能为空")
        private Long toLocId;

        @Schema(description = "来源库存范围", example = "COMMON")
        @InEnum(WmsStockScopeEnum.class)
        private String fromStockScope;

        @Schema(description = "目标库存范围", example = "COMMON")
        @InEnum(WmsStockScopeEnum.class)
        private String toStockScope;

        @Schema(description = "来源项目编号，公共库存传 0", example = "1")
        private Long fromProjectId;

        @Schema(description = "目标项目编号，公共库存传 0", example = "1")
        private Long toProjectId;

        @Schema(description = "来源项目任务编号，无项目任务传 0", example = "1")
        private Long fromProjectTaskId;

        @Schema(description = "目标项目任务编号，无项目任务传 0", example = "1")
        private Long toProjectTaskId;

        @Schema(description = "来源质量状态，留空按合格处理", example = "QUALIFIED")
        @InEnum(WmsQualityStatusEnum.class)
        private String qualityStatusFrom;

        @Schema(description = "目标质量状态，留空沿用来源质量状态", example = "QUALIFIED")
        @InEnum(WmsQualityStatusEnum.class)
        private String qualityStatusTo;

        @Schema(description = "基准计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "基准计量单位编号不能为空")
        private Long uomId;

        @Schema(description = "基准单位编码快照")
        @Size(max = 32, message = "基准单位编码长度不能超过 32")
        private String uomCode;

        @Schema(description = "计划转移数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "计划转移数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "计划转移数量必须大于 0")
        private BigDecimal planQty;

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过 500")
        private String remark;

    }

}
