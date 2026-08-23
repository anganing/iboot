package com.iboot.pf.module.mdm.controller.admin.invorg.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 库存组织分页 Request VO")
@Data
public class InvOrgPageReqVO extends PageParam {

    @Schema(description = "编码")
    private String code;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "状态：0-启用，1-停用")
    private Integer status;
}
