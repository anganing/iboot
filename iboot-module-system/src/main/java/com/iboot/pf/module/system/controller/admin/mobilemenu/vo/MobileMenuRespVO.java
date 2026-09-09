package com.iboot.pf.module.system.controller.admin.mobilemenu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 移动端菜单信息 Response VO")
@Data
public class MobileMenuRespVO {

    @Schema(description = "菜单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "消息通知")
    private String name;

    @Schema(description = "权限标识,仅菜单类型为按钮时，才有值", example = "system:mobile-menu:query")
    private String permission;

    @Schema(description = "类型，参见 MenuTypeEnum 枚举类", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer type;

    @Schema(description = "显示顺序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "父菜单 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Long parentId;

    @Schema(description = "路由地址，uni-app 页面路径", example = "/pages/notify/index")
    private String path;

    @Schema(description = "菜单图标", example = "edit")
    private String icon;

    @Schema(description = "状态,见 CommonStatusEnum 枚举", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer status;

    @Schema(description = "是否可见", example = "true")
    private Boolean visible;

    @Schema(description = "备注", example = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "时间戳格式")
    private LocalDateTime createTime;

    @Schema(description = "子菜单")
    private List<MobileMenuRespVO> children;

}
