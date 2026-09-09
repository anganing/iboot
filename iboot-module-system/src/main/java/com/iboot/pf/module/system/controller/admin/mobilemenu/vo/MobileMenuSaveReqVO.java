package com.iboot.pf.module.system.controller.admin.mobilemenu.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.system.enums.permission.MenuTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "管理后台 - 移动端菜单创建/修改 Request VO")
@Data
public class MobileMenuSaveReqVO {

    @Schema(description = "菜单编号", example = "1")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "消息通知")
    @NotEmpty(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称长度不能超过50个字符")
    private String name;

    @Schema(description = "权限标识,仅菜单类型为按钮时，才需要传递", example = "system:mobile-menu:query")
    @Size(max = 100, message = "权限标识长度不能超过100个字符")
    private String permission;

    @Schema(description = "类型，参见 MenuTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "菜单类型不能为空")
    @InEnum(MenuTypeEnum.class)
    private Integer type;

    @Schema(description = "显示顺序", example = "1")
    private Integer sort;

    @Schema(description = "父菜单 ID，为空时作为根节点", example = "0")
    private Long parentId;

    @Schema(description = "路由地址，uni-app 页面路径,仅菜单类型为入口时，才需要传", example = "/pages/notify/index")
    @Size(max = 200, message = "路由地址不能超过200个字符")
    private String path;

    @Schema(description = "菜单图标,仅菜单类型为分组或者入口时，才需要传", example = "edit")
    @Size(max = 100, message = "菜单图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "状态,见 CommonStatusEnum 枚举", example = "0")
    private Integer status;

    @Schema(description = "是否可见", example = "true")
    private Boolean visible;

    @Schema(description = "备注", example = "备注")
    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

}
