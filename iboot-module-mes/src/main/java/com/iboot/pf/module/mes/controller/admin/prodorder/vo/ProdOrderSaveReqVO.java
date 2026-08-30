package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - MES-生产工单新增/修改 Request VO")
@Data
public class ProdOrderSaveReqVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30486")
    private Long id;

    @Schema(description = "生产工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "生产工单号不能为空")
    private String orderNo;

    @Schema(description = "生产需求编号", example = "32290")
    private Long demandId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8133")
    @NotNull(message = "工厂编号不能为空")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26390")
    @NotNull(message = "车间编号不能为空")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "24435")
    @NotNull(message = "项目编号，0表示无项目不能为空")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4361")
    @NotNull(message = "项目任务编号，0表示无项目任务不能为空")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20684")
    @NotNull(message = "生产物料编号不能为空")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "物料编码快照不能为空")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @NotEmpty(message = "物料名称快照不能为空")
    private String matName;

    @Schema(description = "物料规格快照")
    private String matSpec;

    @Schema(description = "计划生产数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "计划生产数量不能为空")
    private BigDecimal qty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "已完成数量不能为空")
    private BigDecimal completedQty;

    @Schema(description = "累计报废数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "累计报废数量不能为空")
    private BigDecimal scrapQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26955")
    @NotNull(message = "计量单位编号不能为空")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "计量单位编码快照不能为空")
    private String uomCode;

    @Schema(description = "锁定的BOM编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30169")
    @NotNull(message = "锁定的BOM编号不能为空")
    private Long bomId;

    @Schema(description = "锁定的BOM版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "锁定的BOM版本不能为空")
    private String bomVersion;

    @Schema(description = "锁定的工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32358")
    @NotNull(message = "锁定的工艺路线编号不能为空")
    private Long routeId;

    @Schema(description = "锁定的工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "锁定的工艺路线版本不能为空")
    private String routeVersion;

    @Schema(description = "工单交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "工单交期不能为空")
    private LocalDateTime dueDate;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotEmpty(message = "齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE不能为空")
    private String kitStatus;

    @Schema(description = "状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotEmpty(message = "状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED不能为空")
    private String status;

    @Schema(description = "下达时间")
    private LocalDateTime releaseTime;

    @Schema(description = "实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "乐观锁版本不能为空")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    private String remark;

}