package com.iboot.pf.module.wms.controller.admin.invbalance.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - WMS-多维库存余额新增/修改 Request VO")
@Data
public class InvBalanceSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "17435")
    private Long id;

    @Schema(description = "库存组织编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "29559")
    @NotNull(message = "库存组织编号不能为空")
    private Long invOrgId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1418")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "仓库编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "7799")
    @NotNull(message = "仓库编号不能为空")
    private Long whId;

    @Schema(description = "库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "27100")
    @NotNull(message = "库位编号不能为空")
    private Long locId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "16866")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "批次编号；无批次物料挂默认批次", requiredMode = Schema.RequiredMode.REQUIRED, example = "12889")
    @NotNull(message = "批次编号；无批次物料挂默认批次不能为空")
    private Long batchId;

    @Schema(description = "容器编号，0表示散件", requiredMode = Schema.RequiredMode.REQUIRED, example = "18354")
    @NotNull(message = "容器编号，0表示散件不能为空")
    private Long containerId;

    @Schema(description = "项目编号，0表示无项目归属", requiredMode = Schema.RequiredMode.REQUIRED, example = "18162")
    @NotNull(message = "项目编号，0表示无项目归属不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务归属", requiredMode = Schema.RequiredMode.REQUIRED, example = "8693")
    @NotNull(message = "项目任务编号，0表示无项目任务归属不能为空")
    private Long projectTaskId;

    @Schema(description = "库存范围：COMMON、PROJECT_DEDICATED", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库存范围：COMMON、PROJECT_DEDICATED不能为空")
    private String stockScope;

    @Schema(description = "质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN不能为空")
    private String qualityStatus;

    @Schema(description = "现存数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "现存数量，基准单位不能为空")
    private BigDecimal qtyOnhand;

    @Schema(description = "预留数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "预留数量，基准单位不能为空")
    private BigDecimal qtyReserved;

    @Schema(description = "冻结数量，基准单位", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "冻结数量，基准单位不能为空")
    private BigDecimal qtyFrozen;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "基准单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "基准单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "批次号快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "批次号快照不能为空")
    private String lotNo;

    @Schema(description = "仓库编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "仓库编码快照不能为空")
    private String whCode;

    @Schema(description = "库位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库位编码快照不能为空")
    private String locCode;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}