package com.iboot.pf.module.wms.controller.admin.invtxn.vo;


import java.math.BigDecimal;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-库存流水 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvTxnImportExcelVO {

    @ExcelProperty("库存事务编号")
    private String txnNo;

    @ExcelProperty("事务类型：RECEIPT、ISSUE、TRANSFER、RESERVE、RELEASE、FREEZE、UNFREEZE、QUALITY_CHANGE、ADJUST")
    private String txnType;

    @ExcelProperty("来源业务类型")
    private String bizType;

    @ExcelProperty("来源业务单号")
    private String bizNo;

    @ExcelProperty("来源业务行号")
    private String bizLineNo;

    @ExcelProperty("收发方向：IN、OUT、MOVE、STATUS")
    private String ioType;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("工厂编号")
    private Long plantId;

    @ExcelProperty("仓库编号")
    private Long whId;

    @ExcelProperty("来源库位编号，0表示无来源库位")
    private Long fromLocId;

    @ExcelProperty("目标库位编号，0表示无目标库位")
    private Long toLocId;

    @ExcelProperty("物料编号")
    private Long matId;

    @ExcelProperty("批次编号")
    private Long batchId;

    @ExcelProperty("容器编号，0表示散件")
    private Long containerId;

    @ExcelProperty("基准计量单位编号")
    private Long uomId;

    @ExcelProperty("事务数量，使用正数")
    private BigDecimal qty;

    @ExcelProperty("过账前现存数量")
    private BigDecimal beforeQty;

    @ExcelProperty("过账后现存数量")
    private BigDecimal afterQty;

    @ExcelProperty("变更前质量状态")
    private String qualityStatusFrom;

    @ExcelProperty("变更后质量状态")
    private String qualityStatusTo;

    @ExcelProperty("变更前项目编号，0表示无项目")
    private Long projectIdFrom;

    @ExcelProperty("变更前项目任务编号，0表示无项目任务")
    private Long projectTaskIdFrom;

    @ExcelProperty("变更后项目编号，0表示无项目")
    private Long projectIdTo;

    @ExcelProperty("变更后项目任务编号，0表示无项目任务")
    private Long projectTaskIdTo;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("单位编码快照")
    private String uomCode;

    @ExcelProperty("批次号快照")
    private String lotNo;

    @ExcelProperty("来源库位编码快照")
    private String fromLocCode;

    @ExcelProperty("目标库位编码快照")
    private String toLocCode;

    @ExcelProperty("过账幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}