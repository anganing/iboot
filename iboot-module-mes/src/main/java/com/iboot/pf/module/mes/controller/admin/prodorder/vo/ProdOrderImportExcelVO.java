package com.iboot.pf.module.mes.controller.admin.prodorder.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-生产工单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdOrderImportExcelVO {

    @ExcelProperty("生产工单号")
    private String orderNo;

    @ExcelProperty("生产需求编号")
    private Long demandId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("车间编号")
    private Long workshopId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("生产物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("物料规格快照")
    private String matSpec;

    @ExcelProperty("计划生产数量")
    private BigDecimal qty;

    @ExcelProperty("已完成数量")
    private BigDecimal completedQty;

    @ExcelProperty("累计报废数量")
    private BigDecimal scrapQty;

    @ExcelProperty("计量单位编号")
    private Long uomId;

    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @ExcelProperty("锁定的BOM编号")
    private Long bomId;

    @ExcelProperty("锁定的BOM版本")
    private String bomVersion;

    @ExcelProperty("锁定的工艺路线编号")
    private Long routeId;

    @ExcelProperty("锁定的工艺路线版本")
    private String routeVersion;

    @ExcelProperty("工单交期")
    private LocalDateTime dueDate;

    @ExcelProperty("优先级")
    private Integer priority;

    @ExcelProperty("齐套状态：UNCHECKED、FULL_KIT、PARTIAL_KIT、SHORTAGE")
    private String kitStatus;

    @ExcelProperty("状态：DRAFT、RELEASED、READY、RUNNING、HOLD、DONE、CLOSED、CANCELLED")
    private String status;

    @ExcelProperty("下达时间")
    private LocalDateTime releaseTime;

    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}