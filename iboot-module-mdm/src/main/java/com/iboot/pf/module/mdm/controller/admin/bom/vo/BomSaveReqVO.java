package com.iboot.pf.module.mdm.controller.admin.bom.vo;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mdm.dal.dataobject.bomline.BomLineDO;

@Schema(description = "管理后台 - 物料清单新增/修改 Request VO")
@Data
public class BomSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "22921")
    private Long id;

    @Schema(description = "物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3691")
    @NotNull(message = "物料编号不能为空")
    private Long matId;

    @Schema(description = "版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "版本号不能为空")
    private String version;

    @Schema(description = "状态：0-启用，1-停用", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;

    @Schema(description = "生效日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "生效日期不能为空")
    private LocalDate effectiveDate;

    @Schema(description = "失效日期")
    private LocalDate expiryDate;

    @Schema(description = "备注", example = "随便")
    private String remark;

}
