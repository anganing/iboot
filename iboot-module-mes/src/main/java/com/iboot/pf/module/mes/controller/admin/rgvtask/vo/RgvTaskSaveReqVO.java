package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.mes.enums.MesRgvDispatchStatusEnum;
import com.iboot.pf.module.mes.enums.MesRgvTaskStatusEnum;
import com.iboot.pf.module.mes.enums.MesRgvTaskTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * RGV 工位流转任务新增/修改 Request VO。
 *
 * <p>完成时间由外部调度系统回报，乐观锁版本由框架维护，都不由前端提交。
 */
@Schema(description = "管理后台 - MES-RGV 工位流转任务新增/修改 Request VO")
@Data
public class RgvTaskSaveReqVO {

    @Schema(description = "主键", example = "1")
    private Long id;

    @Schema(description = "RGV 本地任务号，留空由编码规则分配")
    @Size(max = 64, message = "本地任务号长度不能超过 64")
    private String taskNo;

    @Schema(description = "RGV 外部任务号")
    @Size(max = 128, message = "外部任务号长度不能超过 128")
    private String externalTaskNo;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "STATION_TRANSFER")
    @NotEmpty(message = "任务类型不能为空")
    @InEnum(MesRgvTaskTypeEnum.class)
    private String taskType;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "作业工单编号不能为空")
    private Long workOrderId;

    @Schema(description = "来源工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源工序任务不能为空")
    private Long opTaskId;

    @Schema(description = "目标工序任务编号，0 表示回库", example = "1")
    private Long targetOpTaskId;

    @Schema(description = "来源工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源工位不能为空")
    private Long fromStationId;

    @Schema(description = "目标工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标工位不能为空")
    private Long toStationId;

    @Schema(description = "载具编号")
    @Size(max = 128, message = "载具编号长度不能超过 128")
    private String carrierNo;

    @Schema(description = "外部调度系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部调度系统编码不能为空")
    @Size(max = 64, message = "外部调度系统编码长度不能超过 64")
    private String externalSystemCode;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @NotEmpty(message = "下发状态不能为空")
    @InEnum(MesRgvDispatchStatusEnum.class)
    private String dispatchStatus;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "CREATED")
    @NotEmpty(message = "执行状态不能为空")
    @InEnum(MesRgvTaskStatusEnum.class)
    private String status;

    @Schema(description = "业务幂等键")
    @Size(max = 128, message = "业务幂等键长度不能超过 128")
    private String idempotencyKey;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;

}
