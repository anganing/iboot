package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** RGV 工位流转任务分页 Request VO。只保留真实会用到的筛选条件。 */
@Schema(description = "管理后台 - MES-RGV 工位流转任务分页 Request VO")
@Data
public class RgvTaskPageReqVO extends PageParam {

    @Schema(description = "RGV 本地任务号")
    private String taskNo;

    @Schema(description = "RGV 外部任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型", example = "STATION_TRANSFER")
    private String taskType;

    @Schema(description = "作业工单编号", example = "1")
    private Long workOrderId;

    @Schema(description = "来源工序任务编号", example = "1")
    private Long opTaskId;

    @Schema(description = "载具编号")
    private String carrierNo;

    @Schema(description = "外部调度系统编码")
    private String externalSystemCode;

    @Schema(description = "下发状态", example = "PENDING")
    private String dispatchStatus;

    @Schema(description = "执行状态", example = "CREATED")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
