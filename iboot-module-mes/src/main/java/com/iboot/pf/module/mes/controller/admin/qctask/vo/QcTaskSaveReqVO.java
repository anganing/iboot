package com.iboot.pf.module.mes.controller.admin.qctask.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesBizTypeEnum;
import com.iboot.pf.module.mes.enums.MesQcConclusionEnum;
import com.iboot.pf.module.mes.enums.MesQcTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcTriggerPointEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 质检任务新增/修改 Request VO。
 *
 * <p>不出现在表单里的字段：物料、单位、批次快照由后端补全；检验方案版本从方案带出；
 * 合格与不合格数量由检验结果汇总；乐观锁版本由框架维护。
 */
@Schema(description = "管理后台 - MES-质检任务新增/修改 Request VO")
@Data
public class QcTaskSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "质检任务号，留空由编码规则分配")
    @Size(max = 64, message = "质检任务号长度不能超过 64")
    private String taskNo;

    @Schema(description = "命中的质检规则编号，0 表示手工建单", example = "1")
    private Long ruleId;

    @Schema(description = "锁定的检验方案编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验方案编号不能为空")
    private Long qcPlanId;

    @Schema(description = "触发点", requiredMode = Schema.RequiredMode.REQUIRED, example = "IPQC")
    @NotEmpty(message = "触发点不能为空")
    @InEnum(MesQcTriggerPointEnum.class)
    private String triggerPoint;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "OP_TASK")
    @NotEmpty(message = "来源业务类型不能为空")
    @InEnum(MesBizTypeEnum.class)
    private String bizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    @Size(max = 64, message = "来源业务单号长度不能超过 64")
    private String bizNo;

    @Schema(description = "来源业务明细编号，0 表示无明细", example = "1")
    private Long bizLineId;

    @Schema(description = "检验物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检验物料编号不能为空")
    private Long matId;

    @Schema(description = "检验批次编号，0 表示不区分批次", example = "1")
    private Long batchId;

    @Schema(description = "送检数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "送检数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "送检数量必须大于零")
    private BigDecimal qty;

    @Schema(description = "抽样数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "抽样数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "抽样数量必须大于零")
    private BigDecimal sampleQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "指派检验员编号，0 表示未指派", example = "1")
    private Long assignedInspectorId;

    @Schema(description = "检验开始时间")
    private LocalDateTime inspectStartTime;

    @Schema(description = "检验结束时间")
    private LocalDateTime inspectEndTime;

    @Schema(description = "检验结论，完成时必填", example = "QUALIFIED")
    @InEnum(MesQcConclusionEnum.class)
    private String conclusion;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "CREATED")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesQcTaskStatusEnum.class)
    private String status;

    @Schema(description = "创建幂等键")
    @Size(max = 128, message = "创建幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
