package com.iboot.pf.module.mes.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 缺陷记录附件 Response VO。文件地址读取时重新签发，避免隔天打不开。 */
@Schema(description = "管理后台 - MES-缺陷记录附件 Response VO")
@Data
public class DefectFileRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "缺陷记录编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long defectId;

    @Schema(description = "附件展示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer seq;

    @Schema(description = "文件类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "IMAGE")
    private String fileType;

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "缺陷照片.png")
    private String fileName;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @Schema(description = "infra 文件编号", example = "1")
    private Long fileId;

}
