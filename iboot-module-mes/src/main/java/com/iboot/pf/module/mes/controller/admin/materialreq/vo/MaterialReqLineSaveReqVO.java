package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesMaterialReqLineStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 领退补料申请明细新增/修改 Request VO。
 *
 * <p>物料、单位、批次快照由后端补全；WMS 已处理数量由 WMS 回写，创建时归零。
 */
@Schema(description = "管理后台 - MES-领退补料申请明细新增/修改 Request VO")
@Data
public class MaterialReqLineSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "领退补料申请编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "领退补料申请编号不能为空")
    private Long reqId;

    @Schema(description = "行号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "行号不能为空")
    @Min(value = 1, message = "行号必须大于零")
    private Integer lineNo;

    @Schema(description = "路线工序用料编号，0 表示非 BOM 用料", example = "1")
    private Long routeOpMatId;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "申请数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "申请数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "申请数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "指定退料批次编号，0 表示不指定批次", example = "1")
    private Long batchId;

    @Schema(description = "是否必须在开工前发料", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    @NotNull(message = "是否必须在开工前发料不能为空")
    private Boolean mustIssueBeforeStart;

    @Schema(description = "明细状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "明细状态不能为空")
    @InEnum(MesMaterialReqLineStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
