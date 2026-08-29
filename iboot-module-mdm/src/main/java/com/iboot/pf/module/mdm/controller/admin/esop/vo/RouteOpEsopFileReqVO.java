package com.iboot.pf.module.mdm.controller.admin.esop.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - ESOP 作业步骤附件 Request VO")
@Data
public class RouteOpEsopFileReqVO {

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "绕线示意图.png")
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过 255")
    private String fileName;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件地址不能为空")
    @Size(max = 512, message = "文件地址长度不能超过 512")
    private String fileUrl;

    @Schema(description = "infra 文件编号")
    private Long fileId;

}
