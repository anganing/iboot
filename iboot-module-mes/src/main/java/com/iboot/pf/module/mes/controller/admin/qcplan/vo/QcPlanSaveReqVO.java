package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - MES-检验方案新增/修改 Request VO")
@Data
public class QcPlanSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "13085")
    private Long id;

    @Schema(description = "检验方案编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "检验方案编码不能为空")
    private String code;

    @Schema(description = "检验方案名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "检验方案名称不能为空")
    private String name;

    @Schema(description = "方案版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "方案版本号不能为空")
    private String versionNo;

    @Schema(description = "抽样类型：FULL、FIXED_QTY、PERCENT、AQL", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "抽样类型：FULL、FIXED_QTY、PERCENT、AQL不能为空")
    private String sampleType;

    @Schema(description = "固定数量或抽样比例")
    private BigDecimal sampleValue;

    @Schema(description = "AQL接收质量限")
    private String aql;

    @Schema(description = "接收判定规则")
    private String acceptanceRule;

    @Schema(description = "状态：DRAFT、PUBLISHED、DISABLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "状态：DRAFT、PUBLISHED、DISABLED不能为空")
    private String status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}