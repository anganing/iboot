package com.iboot.pf.module.wms.controller.admin.arrival.vo;


import java.math.BigDecimal;
import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalSourceTypeEnum;
import com.iboot.pf.module.wms.enums.WmsArrivalStatusEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;

@Schema(description = "管理后台 - WMS-到货登记新增/修改 Request VO")
@Data
public class ArrivalSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "到货登记单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "到货登记单号不能为空")
    private String arrivalNo;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源类型不能为空")
    @InEnum(WmsArrivalSourceTypeEnum.class)
    private String sourceType;

    @Schema(description = "来源单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源单号不能为空")
    private String sourceNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商名称快照")
    private String supplierName;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    @Schema(description = "预计到货日期")
    private LocalDate expectDate;

    @Schema(description = "实际到货时间")
    private LocalDateTime arrivalTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "状态不能为空")
    @InEnum(WmsArrivalStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "到货明细")
    @Valid
    private List<Line> lines;

    /**
     * 到货明细。
     *
     * <p>字段名与 {@code WmsCreateArrivalBO.Line} 逐一对齐，Controller 直接 {@code BeanUtils.toBean} 转换。
     * 不含 {@code status}、{@code qualifiedQty}、{@code rejectedQty}：行状态由领域 Service 写死为 PENDING，
     * 两个数量在质检结论回写时才有值。
     * 不含 {@code matCode}、{@code matName}、{@code matSpec}、{@code uomCode}：这四个快照由领域 Service
     * 从 MaterialApi 与 UomApi 批量取，前端传了也会被覆盖。
     */
    @Schema(description = "管理后台 - WMS-到货登记明细")
    @Data
    public static class Line {

        @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "行号不能为空")
        @Min(value = 1, message = "行号必须大于 0")
        private Integer lineNo;

        @Schema(description = "来源单据行号")
        @Size(max = 64, message = "来源单据行号长度不能超过 64")
        private String sourceLineNo;

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        @Schema(description = "到货批次号，留空时确认到货按默认批次号注册")
        @Size(max = 128, message = "到货批次号长度不能超过 128")
        private String lotNo;

        @Schema(description = "供应商批次号")
        @Size(max = 128, message = "供应商批次号长度不能超过 128")
        private String supplierLotNo;

        @Schema(description = "到货数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "到货数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "到货数量必须大于 0")
        private BigDecimal arrivalQty;

        @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "计量单位编号不能为空")
        private Long uomId;

        @Schema(description = "是否需要质检，留空按不需要处理", example = "false")
        private Boolean needQc;

        @Schema(description = "来料检验模式", example = "PRE_INBOUND")
        @InEnum(WmsIqcModeEnum.class)
        private String iqcMode;

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过 500")
        private String remark;

    }

}
