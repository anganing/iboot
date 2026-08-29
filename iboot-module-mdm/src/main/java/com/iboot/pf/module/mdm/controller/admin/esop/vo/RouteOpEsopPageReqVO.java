package com.iboot.pf.module.mdm.controller.admin.esop.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 工序 ESOP 作业步骤分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RouteOpEsopPageReqVO extends PageParam {

    @Schema(description = "路线工序编号", example = "1")
    private Long routeOpId;

    @Schema(description = "标题", example = "装配要点")
    private String title;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
