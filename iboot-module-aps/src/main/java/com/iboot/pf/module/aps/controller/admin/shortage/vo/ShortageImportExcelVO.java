package com.iboot.pf.module.aps.controller.admin.shortage.vo;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 缺料清单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShortageImportExcelVO {

    @ExcelProperty("排程版本编号")
    private Long versionId;

    @ExcelProperty("生产工单编号，0表示汇总缺料")
    private Long prodOrderId;

    @ExcelProperty("工序任务编号，0表示非工序级缺料")
    private Long opTaskId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("短缺物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @ExcelProperty("可用数量")
    private BigDecimal availQty;

    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @ExcelProperty("计量单位编号")
    private Long uomId;

    @ExcelProperty("计量单位编码快照")
    private String uomCode;

    @ExcelProperty("需求到位时间")
    private LocalDateTime needDate;

    @ExcelProperty("预计供给时间")
    private LocalDateTime expectedSupplyDate;

    @ExcelProperty("状态：OPEN、PARTIAL、RESOLVED、IGNORED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}