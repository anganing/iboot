package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-RGV工位流转任务分页 Request VO")
@Data
public class RgvTaskPageReqVO extends PageParam {

    @Schema(description = "RGV本地任务号")
    private String taskNo;

    @Schema(description = "RGV外部任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN", example = "2")
    private String taskType;

    @Schema(description = "作业工单编号", example = "30010")
    private Long workOrderId;

    @Schema(description = "来源工序任务编号", example = "31323")
    private Long opTaskId;

    @Schema(description = "目标工序任务编号", example = "13527")
    private Long targetOpTaskId;

    @Schema(description = "来源工位或资源编号", example = "12326")
    private Long fromStationId;

    @Schema(description = "目标工位或资源编号", example = "23897")
    private Long toStationId;

    @Schema(description = "载具编号")
    private String carrierNo;

    @Schema(description = "外部调度系统编码")
    private String externalSystemCode;

    @Schema(description = "下发状态：PENDING、SENT、FAILED", example = "1")
    private String dispatchStatus;

    @Schema(description = "执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION", example = "2")
    private String status;

    @Schema(description = "业务幂等键")
    private String idempotencyKey;

    @Schema(description = "完成时间")
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