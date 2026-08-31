package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesDemandSourceTypeEnum;
import com.iboot.pf.module.mes.enums.MesDemandStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产需求新增/修改 Request VO。
 *
 * <p>物料编码、物料名称、物料规格、计量单位编码是快照，由后端在写入时从 mdm 门面取值补全，不由前端提交。
 */
@Schema(description = "管理后台 - MES-生产需求新增/修改 Request VO")
@Data
public class ProdDemandSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "生产需求编号，留空由编码规则分配")
    @Size(max = 64, message = "生产需求编号长度不能超过 64")
    private String demandNo;

    @Schema(description = "来源类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MANUAL")
    @NotEmpty(message = "来源类型不能为空")
    @InEnum(MesDemandSourceTypeEnum.class)
    private String sourceType;

    @Schema(description = "来源单号")
    @Size(max = 64, message = "来源单号长度不能超过 64")
    private String sourceNo;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "项目编号，0 表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目编号不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "项目任务编号不能为空")
    private Long projectTaskId;

    @Schema(description = "需求物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "需求物料编号不能为空")
    private Long matId;

    @Schema(description = "需求数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "需求数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "需求数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "需求交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "需求交期不能为空")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级不能小于零")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesDemandStatusEnum.class)
    private String status;

    @Schema(description = "创建幂等键")
    @Size(max = 128, message = "创建幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
