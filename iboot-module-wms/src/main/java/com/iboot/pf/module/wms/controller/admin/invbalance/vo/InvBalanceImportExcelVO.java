package com.iboot.pf.module.wms.controller.admin.invbalance.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-多维库存余额 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvBalanceImportExcelVO {

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

    @ExcelProperty("批次编号；无批次物料挂默认批次")
    private Long batchId;

    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @ExcelProperty("项目编号，0表示无项目归属")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务归属")
    private Long projectTaskId;

    @ExcelProperty("库存范围：COMMON、PROJECT_DEDICATED")
    private String stockScope;

    @ExcelProperty("质量状态：QUALIFIED、QC_PENDING、REJECTED、FROZEN")
    private String qualityStatus;

    @ExcelProperty("现存数量，基准单位")
    private BigDecimal qtyOnhand;

    @ExcelProperty("预留数量，基准单位")
    private BigDecimal qtyReserved;

    @ExcelProperty("冻结数量，基准单位")
    private BigDecimal qtyFrozen;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("物料规格快照")
    private String matSpec;

    @ExcelProperty("基准单位编码快照")
    private String uomCode;

    @ExcelProperty("批次号快照")
    private String lotNo;

    @ExcelProperty("仓库编码快照")
    private String whCode;

    @ExcelProperty("库位编码快照")
    private String locCode;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}