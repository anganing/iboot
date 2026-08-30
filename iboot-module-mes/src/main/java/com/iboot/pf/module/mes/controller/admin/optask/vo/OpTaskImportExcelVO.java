package com.iboot.pf.module.mes.controller.admin.optask.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-工序任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OpTaskImportExcelVO {

    @ExcelProperty("工序任务号")
    private String taskNo;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("作业工单号快照")
    private String workOrderNo;

    @ExcelProperty("锁定的路线工序编号")
    private Long routeOpId;

    @ExcelProperty("标准工序编号")
    private Long opId;

    @ExcelProperty("工序编码快照")
    private String opCode;

    @ExcelProperty("工序名称快照")
    private String opName;

    @ExcelProperty("工序顺序号")
    private Integer seq;

    @ExcelProperty("并行工序组")
    private String parallelGroup;

    @ExcelProperty("前序工序任务编号")
    private Long prevTaskId;

    @ExcelProperty("后序工序任务编号")
    private Long nextTaskId;

    @ExcelProperty("执行模式：SEQUENTIAL、PARALLEL、OPTIONAL")
    private String execMode;

    @ExcelProperty("质检模式：NONE、BEFORE_START、FIRST_ARTICLE、PROCESS、BEFORE_COMPLETE")
    private String qcMode;

    @ExcelProperty("跳序策略：NOT_ALLOWED、ALLOW_WITH_REASON、APPROVAL_REQUIRED")
    private String skipPolicy;

    @ExcelProperty("是否关键工序")
    private Boolean keyFlag;

    @ExcelProperty("计划数量")
    private BigDecimal plannedQty;

    @ExcelProperty("合格完成数量")
    private BigDecimal completedQty;

    @ExcelProperty("报废数量")
    private BigDecimal scrapQty;

    @ExcelProperty("当前分配资源编号")
    private Long resId;

    @ExcelProperty("计划开始时间")
    private LocalDateTime plannedStartTime;

    @ExcelProperty("计划结束时间")
    private LocalDateTime plannedEndTime;

    @ExcelProperty("实际开始时间")
    private LocalDateTime actualStartTime;

    @ExcelProperty("实际结束时间")
    private LocalDateTime actualEndTime;

    @ExcelProperty("状态：PENDING、READY、RUNNING、PAUSED、QC_WAITING、HOLD、DONE、SKIPPED、CANCELLED")
    private String status;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}