package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;

@Schema(description = "管理后台 - APS-排程任务依赖 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskDepRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "10167")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13102")
    @ExcelProperty("排程版本编号")
    private Long versionId;

    @Schema(description = "前置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18147")
    @ExcelProperty("前置排程任务编号")
    private Long fromTaskId;

    @Schema(description = "后置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "18828")
    @ExcelProperty("后置排程任务编号")
    private Long toTaskId;

    @Schema(description = "依赖类型：FINISH_START、START_START、FINISH_FINISH", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @ExcelProperty("依赖类型：FINISH_START、START_START、FINISH_FINISH")
    private String depType;

    @Schema(description = "依赖间隔分钟数", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("依赖间隔分钟数")
    private Integer lagMinutes;

    @Schema(description = "备注", example = "你说的对")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "排程版本version_no")
    @ExcelProperty("排程版本version_no")
    private String versionVersion_no;

}