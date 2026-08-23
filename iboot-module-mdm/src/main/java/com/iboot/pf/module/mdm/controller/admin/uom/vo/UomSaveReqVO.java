package com.iboot.pf.module.mdm.controller.admin.uom.vo;

import com.iboot.pf.framework.common.enums.CommonStatusEnum;
import com.iboot.pf.framework.common.validation.InEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "管理后台 - 计量单位创建/修改 Request VO")
@Data
public class UomSaveReqVO {
    private Long id;
    @NotBlank(message = "单位编码不能为空")
    @Size(max = 64)
    private String code;
    @NotBlank(message = "单位名称不能为空")
    @Size(max = 128)
    private String name;
    @NotNull(message = "状态不能为空")
    @InEnum(CommonStatusEnum.class)
    private Integer status;
    @Size(max = 500)
    private String remark;
}
