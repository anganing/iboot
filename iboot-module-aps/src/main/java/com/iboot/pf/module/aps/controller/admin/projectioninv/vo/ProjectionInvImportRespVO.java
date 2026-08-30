package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Schema(description = "管理后台 - APS-WMS库存投影导入 Response VO")
@Data
@Builder
public class ProjectionInvImportRespVO {

    @Schema(description = "创建成功的数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    private Integer successCount;

    @Schema(description = "导入失败的数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer failureCount;

    @Schema(description = "导入失败的数据集合，key 为行号，value 为失败原因", requiredMode = Schema.RequiredMode.REQUIRED)
    private Map<Integer, String> failureRows;

}