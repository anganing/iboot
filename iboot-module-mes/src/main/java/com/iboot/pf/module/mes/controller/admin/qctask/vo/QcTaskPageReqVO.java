package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-质检任务分页 Request VO")
@Data
public class QcTaskPageReqVO extends PageParam {

    @Schema(description = "质检任务号")
    private String taskNo;

    @Schema(description = "命中的质检规则编号", example = "1712")
    private Long ruleId;

    @Schema(description = "锁定的检验方案编号", example = "24589")
    private Long qcPlanId;

    @Schema(description = "锁定的检验方案版本")
    private String qcPlanVersion;

    @Schema(description = "触发点：IQC、IPQC、FQC、OQC、FAI")
    private String triggerPoint;

    @Schema(description = "来源业务类型", example = "2")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "来源业务明细编号", example = "8115")
    private Long bizLineId;

    @Schema(description = "检验物料编号", example = "20917")
    private Long matId;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", example = "王五")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "检验批次编号", example = "10315")
    private Long batchId;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "送检数量")
    private BigDecimal qty;

    @Schema(description = "抽样数量")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", example = "28182")
    private Long uomId;

    @Schema(description = "计量单位编码快照")
    private String uomCode;

    @Schema(description = "指派检验员编号", example = "1584")
    private Long assignedInspectorId;

    @Schema(description = "检验开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectStartTime;

    @Schema(description = "检验结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] inspectEndTime;

    @Schema(description = "检验结论：QUALIFIED、REJECTED、CONCESSION")
    private String conclusion;

    @Schema(description = "合格数量")
    private BigDecimal qtyQualified;

    @Schema(description = "不合格数量")
    private BigDecimal qtyRejected;

    @Schema(description = "状态：CREATED、ASSIGNED、INSPECTING、RESULT_POSTING、DONE、CANCELLED", example = "2")
    private String status;

    @Schema(description = "创建幂等键")
    private String idempotencyKey;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}