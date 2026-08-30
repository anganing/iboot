package com.iboot.pf.module.mes.controller.admin.opskiprecord.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-工序跳过记录 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpSkipRecordImportExcelVO {

    @ExcelProperty("被跳过工序任务编号")
    private Long opTaskId;

    @ExcelProperty("跳过原因类型")
    private String skipReasonType;

    @ExcelProperty("跳过原因")
    private String skipReason;

    @ExcelProperty("操作人员编号")
    private Long operatorId;

    @ExcelProperty("审批人员编号")
    private Long approvedBy;

    @ExcelProperty("审批状态：NOT_REQUIRED、PENDING、APPROVED、REJECTED")
    private String approveStatus;

    @ExcelProperty("实际跳过时间")
    private LocalDateTime skipTime;

    @ExcelProperty("对后续工序、质检及排程的影响说明")
    private String effectRemark;

    @ExcelProperty("状态：PENDING、EFFECTIVE、REVOKED")
    private String status;

    @ExcelProperty("备注")
    private String remark;

}