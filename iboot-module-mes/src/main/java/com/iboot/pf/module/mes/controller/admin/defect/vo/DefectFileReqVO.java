package com.iboot.pf.module.mes.controller.admin.defect.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 缺陷记录附件 Request VO。
 *
 * <p>只收前端真正能提供的三个字段；展示顺序与文件类型由后端在写入时生成，前端不传。
 */
@Schema(description = "管理后台 - MES-缺陷记录附件 Request VO")
@Data
public class DefectFileReqVO {

    @Schema(description = "原始文件名", requiredMode = Schema.RequiredMode.REQUIRED, example = "缺陷照片.png")
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名长度不能超过 255")
    private String fileName;

    @Schema(description = "文件地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "文件地址不能为空")
    @Size(max = 512, message = "文件地址长度不能超过 512")
    private String fileUrl;

    @Schema(description = "infra 文件编号", example = "1")
    private Long fileId;

}
