package com.iboot.pf.module.mes.controller.admin.rgvtask.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.mes.enums.MesDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * RGV 工位流转任务 Response VO。
 *
 * <p>工单、工序任务号来自模块内 JOIN；工位名称由 Service 调 mdm 资源门面批量补全。
 */
@Schema(description = "管理后台 - MES-RGV 工位流转任务 Response VO")
@Data
@ExcelIgnoreUnannotated
public class RgvTaskRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "RGV 本地任务号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("本地任务号")
    private String taskNo;

    @Schema(description = "RGV 外部任务号")
    @ExcelProperty("外部任务号")
    private String externalTaskNo;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "STATION_TRANSFER")
    @ExcelProperty(value = "任务类型", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.RGV_TASK_TYPE)
    private String taskType;

    @Schema(description = "作业工单编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workOrderId;

    @Schema(description = "作业工单号")
    @ExcelProperty("作业工单号")
    private String workOrderNo;

    @Schema(description = "来源工序任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long opTaskId;

    @Schema(description = "来源工序任务号")
    @ExcelProperty("来源工序任务号")
    private String opTaskNo;

    @Schema(description = "目标工序任务编号", example = "1")
    private Long targetOpTaskId;

    @Schema(description = "目标工序任务号")
    @ExcelProperty("目标工序任务号")
    private String targetOpTaskNo;

    @Schema(description = "来源工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long fromStationId;

    @Schema(description = "来源工位名称")
    @ExcelProperty("来源工位")
    private String fromStationName;

    @Schema(description = "目标工位或资源编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long toStationId;

    @Schema(description = "目标工位名称")
    @ExcelProperty("目标工位")
    private String toStationName;

    @Schema(description = "载具编号")
    @ExcelProperty("载具编号")
    private String carrierNo;

    @Schema(description = "外部调度系统编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("外部调度系统")
    private String externalSystemCode;

    @Schema(description = "下发状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "PENDING")
    @ExcelProperty(value = "下发状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.RGV_DISPATCH_STATUS)
    private String dispatchStatus;

    @Schema(description = "执行状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "CREATED")
    @ExcelProperty(value = "执行状态", converter = DictConvert.class)
    @DictFormat(MesDictTypeConstants.RGV_TASK_STATUS)
    private String status;

    @Schema(description = "业务幂等键")
    private String idempotencyKey;

    @Schema(description = "完成时间")
    @ExcelProperty("完成时间")
    private LocalDateTime completedTime;

    @Schema(description = "乐观锁版本", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    private Integer version;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
