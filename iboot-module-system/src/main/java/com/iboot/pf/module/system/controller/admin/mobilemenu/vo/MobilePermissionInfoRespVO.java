package com.iboot.pf.module.system.controller.admin.mobilemenu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Schema(description = "管理后台 - 移动端菜单权限信息 Response VO")
@Data
public class MobilePermissionInfoRespVO {

    @Schema(description = "菜单树，只包含分组和入口类型的菜单", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<MobileMenuRespVO> menus;

    @Schema(description = "权限标识集合，包含按钮类型的权限标识", requiredMode = Schema.RequiredMode.REQUIRED)
    private Set<String> permissions;

}
