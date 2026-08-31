package com.iboot.pf.module.mes.controller.admin.workorder.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesWorkOrderStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业工单新增/修改 Request VO。
 *
 * <p>不出现在表单里的字段：生产工单号快照由后端带出；已完成数量由工序任务推进；
 * 实际开工与完工时间由业务动作写入；齐套状态由齐套检查写入；乐观锁版本由框架维护。
 */
@Schema(description = "管理后台 - MES-作业工单新增/修改 Request VO")
@Data
public class WorkOrderSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "作业工单号，留空由编码规则分配")
    @Size(max = 64, message = "作业工单号长度不能超过 64")
    private String workOrderNo;

    @Schema(description = "生产工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产工单编号不能为空")
    private Long prodOrderId;

    @Schema(description = "工作中心编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工作中心编号不能为空")
    private Long wcId;

    @Schema(description = "计划数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划数量必须大于零")
    private BigDecimal plannedQty;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesWorkOrderStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
