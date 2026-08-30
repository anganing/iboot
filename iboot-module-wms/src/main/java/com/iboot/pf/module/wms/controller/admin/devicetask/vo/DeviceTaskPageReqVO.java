package com.iboot.pf.module.wms.controller.admin.devicetask.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-设备搬运任务分页 Request VO")
@Data
public class DeviceTaskPageReqVO extends PageParam {

    @Schema(description = "本地设备任务号")
    private String taskNo;

    @Schema(description = "外部设备任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型：PUTAWAY、PICK、TRANSFER、DELIVERY、RETURN", example = "2")
    private String taskType;

    @Schema(description = "设备类型：WCS、AGV、RGV", example = "2")
    private String deviceType;

    @Schema(description = "来源业务类型", example = "1")
    private String sourceBizType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源业务明细编号", example = "31759")
    private Long sourceLineId;

    @Schema(description = "容器编号，0表示无容器", example = "32170")
    private Long containerId;

    @Schema(description = "来源库位编号", example = "8253")
    private Long fromLocId;

    @Schema(description = "目标库位编号", example = "27764")
    private Long toLocId;

    @Schema(description = "设备回传实际库位编号", example = "32596")
    private Long actualLocId;

    @Schema(description = "外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "下发状态：PENDING、SENT、FAILED", example = "2")
    private String dispatchStatus;

    @Schema(description = "执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION", example = "1")
    private String status;

    @Schema(description = "库存过账状态：PENDING、POSTED、FAILED", example = "1")
    private String postStatus;

    @Schema(description = "下发业务快照")
    private String requestSnapshotJson;

    @Schema(description = "完成回调快照")
    private String callbackSnapshotJson;

    @Schema(description = "业务幂等键")
    private String idempotencyKey;

    @Schema(description = "设备完成时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] completedTime;

    @Schema(description = "乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}