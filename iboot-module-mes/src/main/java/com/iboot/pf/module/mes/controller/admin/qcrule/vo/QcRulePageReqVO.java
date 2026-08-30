package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-质检触发规则分页 Request VO")
@Data
public class QcRulePageReqVO extends PageParam {

    @Schema(description = "质检规则编码")
    private String code;

    @Schema(description = "质检规则名称", example = "赵六")
    private String name;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @Schema(description = "检验对象类型：MATERIAL、BATCH、OP_TASK、PROD_ORDER、OUT_ORDER", example = "2")
    private String objectType;

    @Schema(description = "指定物料编号，0表示不限", example = "21254")
    private Long matId;

    @Schema(description = "指定物料分类编号，0表示不限", example = "13962")
    private Long matCatId;

    @Schema(description = "指定工序编号，0表示不限", example = "3438")
    private Long opId;

    @Schema(description = "指定工作中心编号，0表示不限", example = "27812")
    private Long wcId;

    @Schema(description = "指定供应商编号，0表示不限", example = "2574")
    private Long supplierId;

    @Schema(description = "检验方案编号", example = "13413")
    private Long qcPlanId;

    @Schema(description = "检验模式：PRE_INBOUND、POST_INBOUND_PENDING、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @Schema(description = "优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：0-启用，1-停用", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}