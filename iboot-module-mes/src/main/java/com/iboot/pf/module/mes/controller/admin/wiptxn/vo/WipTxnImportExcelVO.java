package com.iboot.pf.module.mes.controller.admin.wiptxn.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-在制流转流水 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WipTxnImportExcelVO {

    @ExcelProperty("在制事务编号")
    private String txnNo;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("工序任务编号")
    private Long opTaskId;

    @ExcelProperty("事务类型：START、PAUSE、RESUME、REPORT、MOVE、SKIP、COMPLETE、REWORK、SCRAP")
    private String txnType;

    @ExcelProperty("事务数量")
    private BigDecimal qty;

    @ExcelProperty("变更前状态")
    private String fromStatus;

    @ExcelProperty("变更后状态")
    private String toStatus;

    @ExcelProperty("来源业务类型")
    private String bizType;

    @ExcelProperty("来源业务单号")
    private String bizNo;

    @ExcelProperty("来源工序任务编号，0表示无")
    private Long fromOpTaskId;

    @ExcelProperty("目标工序任务编号，0表示无")
    private Long toOpTaskId;

    @ExcelProperty("事务发生时间")
    private LocalDateTime txnTime;

    @ExcelProperty("事务幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}