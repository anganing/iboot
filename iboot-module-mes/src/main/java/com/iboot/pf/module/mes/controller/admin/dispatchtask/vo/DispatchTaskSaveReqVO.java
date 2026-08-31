package com.iboot.pf.module.mes.controller.admin.dispatchtask.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesDispatchTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 派工任务新增/修改 Request VO。
 *
 * <p>作业人员与作业班组至少填一个：派给个人还是派给班组由现场决定，两个都空这条派工没有接收方。
 */
@Schema(description = "管理后台 - MES-派工任务新增/修改 Request VO")
@Data
public class DispatchTaskSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "派工单号，留空由编码规则分配")
    @Size(max = 64, message = "派工单号长度不能超过 64")
    private String dispatchNo;

    @Schema(description = "工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "工序任务编号不能为空")
    private Long opTaskId;

    @Schema(description = "作业人员编号，0 表示派给班组", example = "1")
    private Long operatorId;

    @Schema(description = "作业班组编号，0 表示派给个人", example = "1")
    private Long teamId;

    @Schema(description = "生产资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "生产资源编号不能为空")
    private Long resId;

    @Schema(description = "班次编号，0 表示不区分班次", example = "1")
    private Long shiftId;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "状态不能为空")
    @InEnum(MesDispatchTaskStatusEnum.class)
    private String status;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
