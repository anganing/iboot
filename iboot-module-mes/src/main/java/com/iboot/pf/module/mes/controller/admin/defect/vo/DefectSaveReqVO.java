package com.iboot.pf.module.mes.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Schema(description = "管理后台 - MES-缺陷记录新增/修改 Request VO")
@Data
public class DefectSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "84")
    private Long id;

    @Schema(description = "质检任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "17410")
    @NotNull(message = "质检任务编号不能为空")
    private Long taskId;

    @Schema(description = "关联检验结果编号", example = "23806")
    private Long resultId;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "缺陷编码不能为空")
    private String defectCode;

    @Schema(description = "缺陷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "赵六")
    @NotEmpty(message = "缺陷名称不能为空")
    private String defectName;

    @Schema(description = "缺陷等级：CRITICAL、MAJOR、MINOR")
    private String defectLevel;

    @Schema(description = "缺陷数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "缺陷数量不能为空")
    private BigDecimal qty;

    @Schema(description = "缺陷描述", example = "你猜")
    private String description;

    @Schema(description = "缺陷位置")
    private String location;

    @Schema(description = "缺陷附件地址列表")
    private String attachmentUrls;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

}