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

    @Schema(description = "任务类型", example = "PUTAWAY")
    private String taskType;

    @Schema(description = "设备类型", example = "WCS")
    private String deviceType;

    @Schema(description = "来源业务类型", example = "IN_ORDER")
    private String sourceBizType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "下发状态", example = "PENDING")
    private String dispatchStatus;

    @Schema(description = "执行状态", example = "CREATED")
    private String status;

    @Schema(description = "库存过账状态", example = "PENDING")
    private String postStatus;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
