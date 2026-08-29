package com.iboot.pf.module.mdm.controller.admin.esop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - ESOP 作业步骤附件 Response VO")
@Data
public class RouteOpEsopFileRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "作业步骤编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long esopId;

    @Schema(description = "附件展示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer seq;

    @Schema(description = "文件类型：IMAGE-图片，VIDEO-视频，PDF-PDF文件，DRAWING-图纸，OFFICE-Office文档，OTHER-其他",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "IMAGE")
    private String fileType;

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "绕线示意图.png")
    private String fileName;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;

    @Schema(description = "infra 文件编号")
    private Long fileId;

}
