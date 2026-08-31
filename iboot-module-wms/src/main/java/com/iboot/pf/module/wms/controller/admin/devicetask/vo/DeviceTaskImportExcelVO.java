package com.iboot.pf.module.wms.controller.admin.devicetask.vo;


import java.time.LocalDateTime;
import cn.idev.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WMS-设备搬运任务 Excel 导入 VO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeviceTaskImportExcelVO {

    @ExcelProperty("本地设备任务号")
    private String taskNo;

    @ExcelProperty("外部设备任务号")
    private String externalTaskNo;

    @ExcelProperty("任务类型")
    private String taskType;

    @ExcelProperty("设备类型")
    private String deviceType;

    @ExcelProperty("来源业务类型")
    private String sourceBizType;

    @ExcelProperty("来源业务单号")
    private String sourceBizNo;

    @ExcelProperty("来源业务明细编号")
    private Long sourceLineId;

    @ExcelProperty("容器编号，0表示无容器")
    private Long containerId;

    @ExcelProperty("来源库位编号")
    private Long fromLocId;

    @ExcelProperty("目标库位编号")
    private Long toLocId;

    @ExcelProperty("设备回传实际库位编号")
    private Long actualLocId;

    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @ExcelProperty("下发状态")
    private String dispatchStatus;

    @ExcelProperty("执行状态")
    private String status;

    @ExcelProperty("库存过账状态")
    private String postStatus;

    @ExcelProperty("下发业务快照")
    private String requestSnapshotJson;

    @ExcelProperty("完成回调快照")
    private String callbackSnapshotJson;

    @ExcelProperty("业务幂等键")
    private String idempotencyKey;

    @ExcelProperty("设备完成时间")
    private LocalDateTime completedTime;

    @ExcelProperty("乐观锁版本")
    private Integer version;

    @ExcelProperty("备注")
    private String remark;

}