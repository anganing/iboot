package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - MES-生产工单 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ProdOrderRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "30486")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "生产工单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("生产工单号")
    private String orderNo;

    @Schema(description = "生产需求编号", example = "32290")
    @ExcelProperty("生产需求编号")
    private Long demandId;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8133")
    @ExcelProperty("工厂编号")
    private Long plantId;

    @Schema(description = "车间编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26390")
    @ExcelProperty("车间编号")
    private Long workshopId;

    @Schema(description = "项目编号，0表示无项目", requiredMode = Schema.RequiredMode.REQUIRED, example = "24435")
    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @Schema(description = "项目任务编号，0表示无项目任务", requiredMode = Schema.RequiredMode.REQUIRED, example = "4361")
    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @Schema(description = "生产物料编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "20684")
    @ExcelProperty("生产物料编号")
    private Long matId;

    @Schema(description = "物料编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("物料编码快照")
    private String matCode;

    @Schema(description = "物料名称快照", requiredMode = Schema.RequiredMode.REQUIRED, example = "王五")
    @ExcelProperty("物料名称快照")
    private String matName;

    @Schema(description = "物料规格快照")
    @ExcelProperty("物料规格快照")
    private String matSpec;

    @Schema(description = "计划生产数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划生产数量")
    private BigDecimal qty;

    @Schema(description = "已完成数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @Schema(description = "累计报废数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("累计报废数量")
    private BigDecimal scrapQty;

    @Schema(description = "计量单位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "26955")
    @ExcelProperty("计量单位编号")
    private Long uomId;

    @Schema(description = "计量单位编码快照", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @Schema(description = "锁定的BOM编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "30169")
    @ExcelProperty("锁定的BOM编号")
    private Long bomId;

    @Schema(description = "锁定的BOM版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定的BOM版本")
    private String bomVersion;

    @Schema(description = "锁定的工艺路线编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "32358")
    @ExcelProperty("锁定的工艺路线编号")
    private Long routeId;

    @Schema(description = "锁定的工艺路线版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定的工艺路线版本")
    private String routeVersion;

    @Schema(description = "工单交期", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("工单交期")
    private LocalDateTime dueDate;

    @Schema(description = "优先级", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级")
    private Integer priority;

    @Schema(description = "齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE")
    private String kitStatus;

    @Schema(description = "状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED")
    private String status;

    @Schema(description = "下达时间")
    @ExcelProperty("下达时间")
    private LocalDateTime releaseTime;

    @Schema(description = "实际开始时间")
    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @Schema(description = "实际结束时间")
    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注", example = "你猜")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "生产需求demand_no")
    @ExcelProperty("生产需求demand_no")
    private String demandDemand_no;

}