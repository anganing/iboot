package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MES-RGV工位流转任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RgvTaskImportExcelVO {

    @ExcelProperty("RGV本地任务号")
    private String taskNo;

    @ExcelProperty("RGV外部任务号")
    private String externalTaskNo;

    @ExcelProperty("任务类型：STATION_TRANSFER、BUFFER_TRANSFER、RETURN")
    private String taskType;

    @ExcelProperty("作业工单编号")
    private Long workOrderId;

    @ExcelProperty("来源工序任务编号")
    private Long opTaskId;

    @ExcelProperty("目标工序任务编号")
    private Long targetOpTaskId;

    @ExcelProperty("来源工位或资源编号")
    private Long fromStationId;

    @ExcelProperty("目标工位或资源编号")
    private Long toStationId;

    @ExcelProperty("载具编号")
    private String carrierNo;

    @ExcelProperty("外部调度系统编码")
    private String externalSystemCode;

    @ExcelProperty("下发状态：PENDING、SENT、FAILED")
    private String dispatchStatus;

    @ExcelProperty("执行状态：CREATED、RUNNING、DONE、CANCELLED、EXCEPTION")
    private String status;

    @ExcelProperty("业务幂等键")
    private String idempotencyKey;

    @ExcelProperty("完成时间")
    private LocalDateTime completedTime;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}