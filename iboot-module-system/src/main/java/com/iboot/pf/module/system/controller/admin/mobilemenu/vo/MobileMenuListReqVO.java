package com.iboot.pf.module.system.controller.admin.mobilemenu.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 移动端菜单列表 Request VO")
@Data
public class MobileMenuListReqVO {

    @Schema(description = "菜单名称，模糊匹配", example = "消息")
    private String name;

    @Schema(description = "展示状态，参见 CommonStatusEnum 枚举类", example = "0")
    private Integer status;

}
