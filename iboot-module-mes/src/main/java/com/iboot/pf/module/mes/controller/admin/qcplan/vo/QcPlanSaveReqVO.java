package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesQcPlanStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcSampleTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 检验方案新增/修改 Request VO。
 *
 * <p>抽样值与 AQL 的必填性由抽样类型决定：固定数量、按比例要给抽样值，AQL 抽检要给接收质量限，
 * 这些跨字段规则在 Service 里统一校验。
 */
@Schema(description = "管理后台 - MES-检验方案新增/修改 Request VO")
@Data
public class QcPlanSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "检验方案编码，留空由编码规则分配")
    @Size(max = 64, message = "检验方案编码长度不能超过 64")
    private String code;

    @Schema(description = "检验方案名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验方案名称不能为空")
    @Size(max = 128, message = "检验方案名称长度不能超过 128")
    private String name;

    @Schema(description = "方案版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    @NotEmpty(message = "方案版本号不能为空")
    @Size(max = 32, message = "方案版本号长度不能超过 32")
    private String versionNo;

    @Schema(description = "抽样类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "FULL")
    @NotEmpty(message = "抽样类型不能为空")
    @InEnum(MesQcSampleTypeEnum.class)
    private String sampleType;

    @Schema(description = "固定数量或抽样比例", example = "1")
    @DecimalMin(value = "0", inclusive = false, message = "抽样值必须大于零")
    private BigDecimal sampleValue;

    @Schema(description = "AQL 接收质量限", example = "1.0")
    @Size(max = 32, message = "AQL 长度不能超过 32")
    private String aql;

    @Schema(description = "接收判定规则")
    @Size(max = 500, message = "接收判定规则长度不能超过 500")
    private String acceptanceRule;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesQcPlanStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
