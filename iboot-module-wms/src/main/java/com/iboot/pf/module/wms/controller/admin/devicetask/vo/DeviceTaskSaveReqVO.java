package com.iboot.pf.module.wms.controller.admin.devicetask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import com.iboot.pf.framework.common.validation.InEnum;
import com.iboot.pf.module.wms.enums.WmsDeviceTaskStatusEnum;
import com.iboot.pf.module.wms.enums.WmsDeviceTaskTypeEnum;
import com.iboot.pf.module.wms.enums.WmsDeviceTypeEnum;
import com.iboot.pf.module.wms.enums.WmsDispatchStatusEnum;
import com.iboot.pf.module.wms.enums.WmsPostStatusEnum;
import com.iboot.pf.module.wms.enums.WmsSourceBizTypeEnum;

@Schema(description = "管理后台 - WMS-设备搬运任务新增/修改 Request VO")
@Data
public class DeviceTaskSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "本地设备任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "本地设备任务号不能为空")
    private String taskNo;

    @Schema(description = "外部设备任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "任务类型不能为空")
    @InEnum(WmsDeviceTaskTypeEnum.class)
    private String taskType;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "设备类型不能为空")
    @InEnum(WmsDeviceTypeEnum.class)
    private String deviceType;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务类型不能为空")
    @InEnum(WmsSourceBizTypeEnum.class)
    private String sourceBizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "来源业务单号不能为空")
    private String sourceBizNo;

    @Schema(description = "来源业务明细编号", example = "1")
    private Long sourceLineId;

    @Schema(description = "容器编号，0表示无容器", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "容器编号，0表示无容器不能为空")
    private Long containerId;

    @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "来源库位编号不能为空")
    private Long fromLocId;

    @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "目标库位编号不能为空")
    private Long toLocId;

    @Schema(description = "设备回传实际库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "设备回传实际库位编号不能为空")
    private Long actualLocId;

    @Schema(description = "外部设备系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "外部设备系统编码不能为空")
    private String externalSystemCode;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "下发状态不能为空")
    @InEnum(WmsDispatchStatusEnum.class)
    private String dispatchStatus;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "执行状态不能为空")
    @InEnum(WmsDeviceTaskStatusEnum.class)
    private String status;

    @Schema(description = "库存过账状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "库存过账状态不能为空")
    @InEnum(WmsPostStatusEnum.class)
    private String postStatus;

    @Schema(description = "下发业务快照")
    private String requestSnapshotJson;

    @Schema(description = "完成回调快照")
    private String callbackSnapshotJson;

    @Schema(description = "业务幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "业务幂等键不能为空")
    private String idempotencyKey;

    @Schema(description = "设备完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注")
    private String remark;

}
