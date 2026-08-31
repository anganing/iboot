package com.iboot.pf.module.aps.controller.admin.tasklock.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程任务锁定记录 Response VO。
 *
 * <p>任务编号、工序、资源编码由 Mapper JOIN {@code aps_task} 取出；锁定人昵称由 Service 走 system 门面补全。
 */
@Schema(description = "管理后台 - APS-排程任务锁定记录 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskLockRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long taskId;

    @Schema(description = "排程任务号")
    @ExcelProperty("排程任务号")
    private String taskNo;

    @Schema(description = "工序编码")
    @ExcelProperty("工序编码")
    private String opCode;

    @Schema(description = "工序名称")
    @ExcelProperty("工序名称")
    private String opName;

    @Schema(description = "资源编码")
    @ExcelProperty("资源编码")
    private String resCode;

    @Schema(description = "锁定类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "MANUAL")
    @ExcelProperty(value = "锁定类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.TASK_LOCK_TYPE)
    private String lockType;

    @Schema(description = "锁定原因", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定原因")
    private String reason;

    @Schema(description = "锁定人员编号", example = "1")
    private Long lockedBy;

    @Schema(description = "锁定人")
    @ExcelProperty("锁定人")
    private String lockedByName;

    @Schema(description = "锁定时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("锁定时间")
    private LocalDateTime lockedTime;

    @Schema(description = "解锁时间")
    @ExcelProperty("解锁时间")
    private LocalDateTime unlockTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "LOCKED")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.TASK_LOCK_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
