package com.iboot.pf.module.aps.controller.admin.projectionorder.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/** 工单投影分页 Request VO。 */
@Schema(description = "管理后台 - APS-工单投影分页 Request VO")
@Data
public class ProjectionOrderPageReqVO extends PageParam {

    @Schema(description = "排程场景编号", example = "1")
    private Long scenarioId;

    @Schema(description = "来源类型", example = "OP_TASK")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "生产物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码")
    private String matCode;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "项目编号", example = "1")
    private Long projectId;

    @Schema(description = "当前资源编号", example = "1")
    private Long resId;

    @Schema(description = "来源对象状态", example = "PENDING")
    private String currentStatus;

    @Schema(description = "是否已开工或业务锁定")
    private Boolean lockedFlag;

    @Schema(description = "交期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] dueDate;

    @Schema(description = "投影快照时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] snapshotTime;

}
