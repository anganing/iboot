package com.iboot.pf.module.mdm.controller.admin.route.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Schema(description = "管理后台 - 工艺路线编排详情 Response VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RouteDetailRespVO extends RouteRespVO {

    @Schema(description = "工艺路线工序")
    private List<RouteOpRespVO> operations;

}
