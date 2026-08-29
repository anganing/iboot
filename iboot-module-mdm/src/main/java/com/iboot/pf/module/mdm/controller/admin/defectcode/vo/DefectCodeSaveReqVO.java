package com.iboot.pf.module.mdm.controller.admin.defectcode.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 缺陷代码新增/修改 Request VO")
@Data
public class DefectCodeSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "缺陷编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "SCRATCH")
    @NotBlank(message = "缺陷编码不能为空")
    private String code;

    @Schema(description = "缺陷名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "表面划伤")
    @NotBlank(message = "缺陷名称不能为空")
    private String name;

    @Schema(description = "缺陷等级：CRITICAL-致命，MAJOR-严重，MINOR-轻微",
            requiredMode = Schema.RequiredMode.REQUIRED, example = "MAJOR")
    @NotBlank(message = "缺陷等级不能为空")
    @Pattern(regexp = "CRITICAL|MAJOR|MINOR", message = "缺陷等级不合法")
    private String level;

    @Schema(description = "适用工序编号列表，为空表示不限")
    private List<Long> opIds;

    @Schema(description = "适用物料分类编号列表，为空表示不限")
    private List<Long> matCatIds;

    @Schema(description = "缺陷分类树父级编号，0-顶级", example = "0")
    private Long parentId;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

}
