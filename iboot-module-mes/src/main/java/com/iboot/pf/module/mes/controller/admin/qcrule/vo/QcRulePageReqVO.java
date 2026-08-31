package com.iboot.pf.module.mes.controller.admin.qcrule.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 质检触发规则分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-质检触发规则分页 Request VO")
@Data
public class QcRulePageReqVO extends PageParam {

    @Schema(description = "质检规则编码")
    private String code;

    @Schema(description = "质检规则名称")
    private String name;

    @Schema(description = "触发点", example = "IQC")
    private String triggerPoint;

    @Schema(description = "检验对象类型", example = "MATERIAL")
    private String objectType;

    @Schema(description = "指定物料编号", example = "1")
    private Long matId;

    @Schema(description = "指定工序编号", example = "1")
    private Long opId;

    @Schema(description = "指定工作中心编号", example = "1")
    private Long wcId;

    @Schema(description = "指定供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "检验方案编号", example = "1")
    private Long qcPlanId;

    @Schema(description = "检验模式", example = "PROCESS")
    private String qcMode;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
