package com.iboot.pf.module.mdm.controller.admin.wc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;

@Schema(description = "管理后台 - 工作中心新增/修改 Request VO")
@Data
public class WcSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "9915")
    private Long id;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9908")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

}