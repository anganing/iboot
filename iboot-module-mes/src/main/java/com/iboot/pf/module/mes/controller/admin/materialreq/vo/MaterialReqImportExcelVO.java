package com.iboot.pf.module.mes.controller.admin.materialreq.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-领退补料申请 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MaterialReqImportExcelVO {

    @ExcelProperty("领退补料申请单号")
    private String reqNo;

    @ExcelProperty("生产工单编号")
    private Long prodOrderId;

    @ExcelProperty("生产工单号快照")
    private String prodOrderNo;

    @ExcelProperty("作业工单编号，0表示工单级申请")
    private Long workOrderId;

    @ExcelProperty("工序任务编号，0表示非工序级申请")
    private Long opTaskId;

    @ExcelProperty("申请类型：ISSUE、RETURN、SUPPLEMENT")
    private String reqType;

    @ExcelProperty("库存组织编号")
    private Long invOrgId;

    @ExcelProperty("目标仓库或来源仓库编号")
    private Long whId;

    @ExcelProperty("项目编号，0表示无项目")
    private Long projectId;

    @ExcelProperty("项目任务编号，0表示无项目任务")
    private Long projectTaskId;

    @ExcelProperty("WMS生成的出入库单号")
    private String wmsBizNo;

    @ExcelProperty("状态：DRAFT、SUBMITTED、WMS_PROCESSING、PARTIAL、DONE、CANCELLED")
    private String status;

    @ExcelProperty("创建幂等键")
    private String idempotencyKey;

    @ExcelProperty("备注")
    private String remark;

}