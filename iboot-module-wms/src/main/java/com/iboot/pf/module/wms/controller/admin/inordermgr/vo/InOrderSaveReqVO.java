package com.iboot.pf.module.wms.controller.admin.inordermgr.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsHookModeEnum;
import com.iboot.pf.module.wms.enums.WmsInBizTypeEnum;
import com.iboot.pf.module.wms.enums.WmsIqcModeEnum;
import com.iboot.pf.module.wms.enums.WmsOrderStatusEnum;
import com.iboot.pf.module.wms.enums.WmsOriginTypeEnum;
import com.iboot.pf.module.wms.enums.WmsQualityStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceModuleEnum;

@Schema(description = "管理后台 - WMS-入库单新增/修改 Request VO")
@Data
public class InOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "入库单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库单号不能为空")
    private String inNo;

    @Schema(description = "入库业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库业务类型不能为空")
    @InEnum(WmsInBizTypeEnum.class)
    private String bizType;

    @Schema(description = "直接来源类型")
    @InEnum(WmsSourceModuleEnum.class)
    private String sourceType;

    @Schema(description = "直接来源业务单号")
    private String sourceBizNo;

    @Schema(description = "直接来源明细编号", example = "1")
    private Long sourceLineId;

    @Schema(description = "原始来源类型")
    @InEnum(WmsOriginTypeEnum.class)
    private String originType;

    @Schema(description = "原始来源单号")
    private String originNo;

    @Schema(description = "到货登记编号", example = "1")
    private Long arrivalId;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "客户编号", example = "1")
    private Long customerId;

    @Schema(description = "生产工单号快照")
    private String prodOrderNo;

    @Schema(description = "作业工单号快照")
    private String workOrderNo;

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "盘点单号")
    private String countOrderNo;

    @Schema(description = "目标库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "目标工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "目标仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标仓库编号不能为空")
    private Long whId;

    @Schema(description = "目标仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "目标仓库编码快照不能为空")
    private String whCode;

    @Schema(description = "来源库存组织编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库存组织编号，0表示非调拨不能为空")
    private Long fromInvOrgId;

    @Schema(description = "来源仓库编号，0表示非调拨", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源仓库编号，0表示非调拨不能为空")
    private Long fromWhId;

    @Schema(description = "是否需要质检", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否需要质检不能为空")
    private Boolean needQc;

    @Schema(description = "来料检验模式")
    @InEnum(WmsIqcModeEnum.class)
    private String iqcMode;

    @Schema(description = "入库质量状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "入库质量状态不能为空")
    @InEnum(WmsQualityStatusEnum.class)
    private String qualityStatus;

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

    @Schema(description = "低频扩展属性")
    private String bizAttrsJson;

    @Schema(description = "备注或人工原因")
    private String remark;

    @Schema(description = "入库明细")
    @Valid
    private List<Line> lines;

    /**
     * 入库明细。
     *
     * <p>字段名与 {@code WmsCreateInOrderBO.Line} 逐一对齐，Controller 直接 {@code BeanUtils.toBean} 转换。
     * 不含 {@code status}：行状态由领域 Service 写死为 DRAFT。
     * 不含 {@code matCode}、{@code matName}、{@code matSpec}：物料快照由领域 Service 从 MaterialApi 取，前端传了也会被覆盖。
     */
    @Schema(description = "管理后台 - WMS-入库单明细")
    @Data
    public static class Line {

        @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
        @NotNull(message = "行号不能为空")
        @Min(value = 1, message = "行号必须大于 0")
        private Integer lineNo;

        @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "物料编号不能为空")
        private Long matId;

        @Schema(description = "批次编号，留空由领域 Service 按批次号注册", example = "1")
        private Long batchId;

        @Schema(description = "批次号，无批次物料留空取默认批次")
        @Size(max = 64, message = "批次号长度不能超过 64")
        private String lotNo;

        @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "计量单位编号不能为空")
        private Long uomId;

        @Schema(description = "计量单位编码快照")
        @Size(max = 32, message = "计量单位编码长度不能超过 32")
        private String uomCode;

        @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
        @NotNull(message = "目标库位编号不能为空")
        private Long locId;

        @Schema(description = "目标库位编码快照")
        @Size(max = 64, message = "目标库位编码长度不能超过 64")
        private String locCode;

        @Schema(description = "容器编号，散件留空", example = "1")
        private Long containerId;

        @Schema(description = "入库质量状态，留空沿用单据质量状态", example = "QUALIFIED")
        @InEnum(WmsQualityStatusEnum.class)
        private String qualityStatus;

        @Schema(description = "计划入库数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
        @NotNull(message = "计划入库数量不能为空")
        @DecimalMin(value = "0", inclusive = false, message = "计划入库数量必须大于 0")
        private BigDecimal planQty;

        @Schema(description = "供应商编号", example = "1")
        private Long supplierId;

        @Schema(description = "供应商编码快照")
        @Size(max = 64, message = "供应商编码长度不能超过 64")
        private String supplierCode;

        @Schema(description = "供应商批次号")
        @Size(max = 64, message = "供应商批次号长度不能超过 64")
        private String supplierLotNo;

        @Schema(description = "炉批号")
        @Size(max = 64, message = "炉批号长度不能超过 64")
        private String furnaceLotNo;

        @Schema(description = "生产日期")
        private LocalDate produceDate;

        @Schema(description = "失效日期")
        private LocalDate expiryDate;

        @Schema(description = "备注")
        @Size(max = 500, message = "备注长度不能超过 500")
        private String remark;

    }

}
