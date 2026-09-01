package com.iboot.pf.module.mdm.controller.admin.res.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 生产资源精简列表 Request VO")
@Data
public class ResSimpleListReqVO {

    @Schema(description = "资源编号列表，为空表示不限定", example = "5732,5733")
    private List<Long> ids;

    @Schema(description = "工作中心编号", example = "20144")
    private Long wcId;

    @Schema(description = "资源类型", example = "1")
    private String resType;

    @Schema(description = "状态", example = "0")
    private Integer status;

}
