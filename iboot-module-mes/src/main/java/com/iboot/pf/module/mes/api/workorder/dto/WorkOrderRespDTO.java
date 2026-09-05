package com.iboot.pf.module.mes.api.workorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 作业工单跨模块只读事实。
 *
 * <p>作业工单是生产工单按工作中心切出来的执行段，不允许手工新增修改，因此门面只给读。
 */
@Schema(description = "RPC 服务 - 作业工单 Response DTO")
@Data
public class WorkOrderRespDTO {

    @Schema(description = "作业工单编号", example = "1")
    private Long id;

    @Schema(description = "作业工单号", example = "WO202609040001")
    private String workOrderNo;

    @Schema(description = "生产工单编号", example = "1")
    private Long prodOrderId;

    @Schema(description = "生产工单号快照", example = "PO202609040001")
    private String prodOrderNo;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "工作中心编号", example = "1")
    private Long wcId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "计划数量", example = "100")
    private BigDecimal plannedQty;

    @Schema(description = "已完成数量", example = "20")
    private BigDecimal completedQty;

    @Schema(description = "计划开始时间")
    private LocalDateTime plannedStartTime;

    @Schema(description = "计划结束时间")
    private LocalDateTime plannedEndTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    /** 齐套状态，取值为 MesKitStatusEnum 的枚举名。 */
    @Schema(description = "齐套状态", example = "FULL_KIT")
    private String kitStatus;

    /** 作业工单状态，取值为 MesWorkOrderStatusEnum 的枚举名。 */
    @Schema(description = "作业工单状态", example = "RUNNING")
    private String status;

}
