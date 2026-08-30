package com.iboot.pf.module.aps.controller.admin.projectioninv.vo;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * APS-WMS库存投影 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectionInvImportExcelVO {

    @ExcelProperty("排程场景编号")
    private Long scenarioId;

    @ExcelProperty("WMS库存余额编号")
    private Long sourceBalanceId;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("库位编号")
    private Long locId;

    @ExcelProperty("物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("批次编号")
    private Long batchId;

    @ExcelProperty("批次号快照")
    private String lotNo;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("库存范围：COMMON、PROJECT_DEDICATED")
    private String stockScope;

    @ExcelProperty("质量状态")
    private String qualityStatus;

    @ExcelProperty("现存数量快照")
    private BigDecimal qtyOnhand;

    @ExcelProperty("预留数量快照")
    private BigDecimal qtyReserved;

    @ExcelProperty("冻结数量快照")
    private BigDecimal qtyFrozen;

    @ExcelProperty("可用数量快照")
    private BigDecimal qtyAvailable;

    @ExcelProperty("来源数据更新时间")
    private LocalDateTime sourceUpdateTime;

    @ExcelProperty("投影快照时间")
    private LocalDateTime snapshotTime;

    @ExcelProperty("备注")
    private String remark;

}