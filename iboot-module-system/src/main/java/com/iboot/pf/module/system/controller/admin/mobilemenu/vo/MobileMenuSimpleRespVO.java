package com.iboot.pf.module.system.controller.admin.mobilemenu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 移动端菜单精简信息 Response VO")
@Data
public class MobileMenuSimpleRespVO {

    @Schema(description = "菜单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "消息通知")
    private String name;

    @Schema(description = "父菜单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "类型，参见 MenuTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "状态,见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

}
