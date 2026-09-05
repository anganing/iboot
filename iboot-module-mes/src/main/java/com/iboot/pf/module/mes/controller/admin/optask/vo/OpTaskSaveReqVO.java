package com.iboot.pf.module.mes.controller.admin.optask.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesOpTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesQcModeEnum;
import com.iboot.pf.module.mes.enums.MesSkipPolicyEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 工序任务新增/修改 Request VO。
 *
 * <p>不出现在表单里的字段：作业工单号、工序编码与名称、工序顺序、并行组、关键工序标记、执行模式
 * 都由后端从作业工单与路线工序带出；完成与报废数量由报工推进；实际时间由业务动作写入；
 * 前后序任务编号由工序链生成时维护。
 *
 * <p><b>状态不在表单里</b>：工序任务状态只能由执行动作推进（开工、暂停、恢复、完工、跳序），
 * 创建时由 Service 兜底为 {@link MesOpTaskStatusEnum#PENDING}，改单时保持原值。
 */
@Schema(description = "管理后台 - MES-工序任务新增/修改 Request VO")
@Data
public class OpTaskSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "工序任务号，留空由编码规则分配")
    @Size(max = 64, message = "工序任务号长度不能超过 64")
    private String taskNo;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "锁定的路线工序编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "路线工序编号不能为空")
    private Long routeOpId;

    @Schema(description = "质检模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "NONE")
    @NotEmpty(message = "质检模式不能为空")
    @InEnum(MesQcModeEnum.class)
    private String qcMode;

    @Schema(description = "跳序策略", requiredMode = Schema.RequiredMode.REQUIRED, example = "NOT_ALLOWED")
    @NotEmpty(message = "跳序策略不能为空")
    @InEnum(MesSkipPolicyEnum.class)
    private String skipPolicy;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划数量必须大于零")
    private BigDecimal plannedQty;

    @Schema(description = "分配资源编号，0 表示未分配", example = "1")
    private Long resId;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
