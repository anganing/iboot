package com.iboot.pf.module.wms.controller.admin.devicetask.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;

@Schema(description = "管理后台 - WMS-设备搬运任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class DeviceTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "本地设备任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本地设备任务号")
    private String taskNo;

    @Schema(description = "外部设备任务号")
    @ExcelProperty("外部设备任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "任务类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.DEVICE_TASK_TYPE)
    private String taskType;

    @Schema(description = "设备类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "设备类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.DEVICE_TYPE)
    private String deviceType;

    @Schema(description = "来源业务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "来源业务类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.SOURCE_BIZ_TYPE)
    private String sourceBizType;

    @Schema(description = "来源业务单号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源业务明细编号", example = "1")
    @ExcelProperty("来源业务明细编号")
    private Long sourceLineId;

    @Schema(description = "容器编号，0表示无容器", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("容器编号，0表示无容器")
    private Long containerId;

    @Schema(description = "来源库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("来源库位编号")
    private Long fromLocId;

    @Schema(description = "目标库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("目标库位编号")
    private Long toLocId;

    @Schema(description = "设备回传实际库位编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("设备回传实际库位编号")
    private Long actualLocId;

    @Schema(description = "外部设备系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部设备系统编码")
    private String externalSystemCode;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "下发状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.DISPATCH_STATUS)
    private String dispatchStatus;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "执行状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.DEVICE_TASK_STATUS)
    private String status;

    @Schema(description = "库存过账状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "库存过账状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.POST_STATUS)
    private String postStatus;

    @Schema(description = "下发业务快照")
    @ExcelProperty("下发业务快照")
    private String requestSnapshotJson;

    @Schema(description = "完成回调快照")
    @ExcelProperty("完成回调快照")
    private String callbackSnapshotJson;

    @Schema(description = "业务幂等键", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("业务幂等键")
    private String idempotencyKey;

    @Schema(description = "设备完成时间")
    @ExcelProperty("设备完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("乐观锁版本")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

    @Schema(description = "搬运容器编号")
    @ExcelProperty("搬运容器编号")
    private String containerNo;

    @Schema(description = "来源库位编码")
    @ExcelProperty("来源库位编码")
    private String fromLocCode;

    @Schema(description = "目标库位编码")
    @ExcelProperty("目标库位编码")
    private String toLocCode;

    @Schema(description = "实际库位编码")
    @ExcelProperty("实际库位编码")
    private String actualLocCode;

}
