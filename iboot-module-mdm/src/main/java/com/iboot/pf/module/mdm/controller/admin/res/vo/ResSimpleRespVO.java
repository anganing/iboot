package com.iboot.pf.module.mdm.controller.admin.res.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 生产资源精简 Response VO")
@Data
public class ResSimpleRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "32120")
    private Long id;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    private String name;

    @Schema(description = "资源类型", example = "1")
    private String resType;

    @Schema(description = "工作中心编号", example = "20144")
    private Long wcId;

    @Schema(description = "工作中心名称")
    private String wcName;

}
