package com.iboot.pf.module.mes.controller.admin.disposition.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-不合格品处置单 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DispositionImportExcelVO {

    @ExcelProperty("不合格处置单号")
    private String dispositionNo;

    @ExcelProperty("质检任务编号")
    private Long taskId;

    @ExcelProperty("处置类型：REWORK、SCRAP、CONCESSION、FREEZE、RETURN_SUPPLIER、ADJUST_PROCESS")
    private String dispositionType;

    @ExcelProperty("处置数量")
    private BigDecimal qty;

    @ExcelProperty("联动目标业务类型")
    private String targetBizType;

    @ExcelProperty("联动目标业务单号")
    private String targetBizNo;

    @ExcelProperty("审批人员编号")
    private Long approvedBy;

    @ExcelProperty("审批时间")
    private LocalDateTime approveTime;

    @ExcelProperty("状态：DRAFT、SUBMITTED、APPROVED、EXECUTING、DONE、REJECTED、CANCELLED")
    private String status;

    @ExcelProperty("处置幂等键")
    private String idempotencyKey;

    @ExcelProperty("处置说明")
    private String remark;

}