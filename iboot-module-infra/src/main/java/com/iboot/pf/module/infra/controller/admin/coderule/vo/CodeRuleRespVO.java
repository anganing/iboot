package com.iboot.pf.module.infra.controller.admin.coderule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 编码规则 Response VO")
@Data
public class CodeRuleRespVO {

    private Long id;
    private String ruleCode;
    private String name;
    private String prefix;
    private Boolean yearEnabled;
    private Boolean monthEnabled;
    private Boolean dayEnabled;
    private String suffix;
    private Integer serialLength;
    private Long serialStart;
    private Long serialStep;
    private Integer resetCycle;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
