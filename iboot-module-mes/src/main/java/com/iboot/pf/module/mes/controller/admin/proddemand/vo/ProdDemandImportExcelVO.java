package com.iboot.pf.module.mes.controller.admin.proddemand.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-生产需求 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProdDemandImportExcelVO {

    @ExcelProperty("生产需求编号")
    private String demandNo;

    @ExcelProperty("来源类型：MANUAL、EXCEL、ERP、APS")
    private String sourceType;

    @ExcelProperty("来源单号")
    private String sourceNo;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("需求物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("物料规格快照")
    private String matSpec;

    @ExcelProperty("需求数量")
    private BigDecimal qty;

    @ExcelProperty("计量单位编号")
    private Long uomId;

    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @ExcelProperty("需求交期")
    private LocalDateTime dueDate;

    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @ExcelProperty("状态：DRAFT、CONFIRMED、PLANNED、RELEASED、DONE、CANCELLED")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}