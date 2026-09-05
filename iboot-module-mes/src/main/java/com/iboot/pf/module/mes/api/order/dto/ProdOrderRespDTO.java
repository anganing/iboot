package com.iboot.pf.module.mes.api.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 生产工单跨模块只读事实。
 *
 * <p>{@code bomId} {@code bomVersion} {@code routeId} {@code routeVersion} 四个字段是下达时锁定的版本快照，
 * APS 排程要靠它们确定"这张工单按哪一版 BOM 与工艺路线做"，追溯查询也靠它们回溯当时的结构，缺一个都不行。
 */
@Schema(description = "RPC 服务 - 生产工单 Response DTO")
@Data
public class ProdOrderRespDTO {

    @Schema(description = "生产工单编号", example = "1")
    private Long id;

    @Schema(description = "生产工单号", example = "PO202609040001")
    private String orderNo;

    @Schema(description = "生产需求编号，0 表示手工建单", example = "1")
    private Long demandId;

    @Schema(description = "工厂编号", example = "1")
    private Long plantId;

    @Schema(description = "车间编号", example = "1")
    private Long workshopId;

    @Schema(description = "项目编号，0 表示无项目", example = "1")
    private Long projectId;

    @Schema(description = "项目任务编号，0 表示无项目任务", example = "1")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", example = "1")
    private Long matId;

    @Schema(description = "物料编码快照", example = "M-0001")
    private String matCode;

    @Schema(description = "物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "计划生产数量", example = "100")
    private BigDecimal qty;

    @Schema(description = "已完成数量", example = "20")
    private BigDecimal completedQty;

    @Schema(description = "累计报废数量", example = "1")
    private BigDecimal scrapQty;

    @Schema(description = "计量单位编号", example = "1")
    private Long uomId;

    @Schema(description = "计量单位编码快照", example = "PCS")
    private String uomCode;

    @Schema(description = "锁定的 BOM 编号", example = "1")
    private Long bomId;

    @Schema(description = "锁定的 BOM 版本快照", example = "V1.0")
    private String bomVersion;

    @Schema(description = "锁定的工艺路线编号", example = "1")
    private Long routeId;

    @Schema(description = "锁定的工艺路线版本快照", example = "V1.0")
    private String routeVersion;

    @Schema(description = "工单交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级，数值越大越优先", example = "10")
    private Integer priority;

    /** 齐套状态，取值为 MesKitStatusEnum 的枚举名。 */
    @Schema(description = "齐套状态", example = "FULL_KIT")
    private String kitStatus;

    /** 工单状态，取值为 MesProdOrderStatusEnum 的枚举名。 */
    @Schema(description = "工单状态", example = "RELEASED")
    private String status;

    @Schema(description = "下达时间")
    private LocalDateTime releaseTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

}
