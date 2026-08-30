package com.iboot.pf.module.mes.controller.admin.kitsnapshot.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-齐套检查快照 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KitSnapshotImportExcelVO {

    @ExcelProperty("齐套检查编号")
    private String checkNo;

    @ExcelProperty("检查范围：PROD_ORDER、WORK_ORDER、OP_TASK")
    private String checkScope;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("作业工单编号，0表示整单检查")
    private Long workOrderId;

    @ExcelProperty("工序任务编号，0表示非工序检查")
    private Long opTaskId;

    @ExcelProperty("检查明细行号")
    private Integer lineNo;

    @ExcelProperty("需求物料编号")
    private Long matId;

    @ExcelProperty("物料编码快照")
    private String matCode;

    @ExcelProperty("物料名称快照")
    private String matName;

    @ExcelProperty("需求数量")
    private BigDecimal reqQty;

    @ExcelProperty("检查时可用数量")
    private BigDecimal availQty;

    @ExcelProperty("检查时本需求已预留数量")
    private BigDecimal reservedQty;

    @ExcelProperty("检查时已领用数量")
    private BigDecimal issuedQty;

    @ExcelProperty("短缺数量")
    private BigDecimal shortQty;

    @ExcelProperty("实际采用替代料编号，0表示未采用")
    private Long altMatId;

    @ExcelProperty("检查结果：FULL_KIT、PARTIAL_KIT、SHORTAGE")
    private String result;

    @ExcelProperty("检查时间")
    private LocalDateTime checkTime;

    @ExcelProperty("备注")
    private String remark;

}