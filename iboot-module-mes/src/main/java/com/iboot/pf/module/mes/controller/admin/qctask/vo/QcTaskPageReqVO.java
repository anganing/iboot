package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 质检任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-质检任务分页 Request VO")
@Data
public class QcTaskPageReqVO extends PageParam {

    @Schema(description = "质检任务号")
    private String taskNo;

    @Schema(description = "命中的质检规则编号", example = "1")
    private Long ruleId;

    @Schema(description = "检验方案编号", example = "1")
    private Long qcPlanId;

    @Schema(description = "触发点", example = "IPQC")
    private String triggerPoint;

    @Schema(description = "来源业务类型", example = "OP_TASK")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "检验物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "物料名称")
    private String matName;

    @Schema(description = "批次号")
    private String lotNo;

    @Schema(description = "指派检验员编号", example = "1")
    private Long assignedInspectorId;

    @Schema(description = "检验结论", example = "QUALIFIED")
    private String conclusion;

    @Schema(description = "状态", example = "CREATED")
    private String status;

    @Schema(description = "检验开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectStartTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
