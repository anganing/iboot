package com.iboot.pf.module.mdm.controller.admin.route.vo;

import com.iboot.pf.module.mdm.controller.admin.routeopmat.vo.RouteOpMatRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 工艺路线工序 Response VO")
@Data
public class RouteOpRespVO {

    private Long id;
    private Long routeId;
    private Integer seq;
    private Long opId;
    private String opCode;
    private String opName;
    private Long wcId;
    private String wcCode;
    private String wcName;
    private String parallelGroup;
    private Boolean keyFlag;
    private Boolean faiFlag;
    private String skipPolicy;
    private String remark;
    private LocalDateTime createTime;
    private Integer materialCount;
    private List<RouteOpMatRespVO> materials;

}
