package com.iboot.pf.module.aps.controller.admin.taskdep.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程任务依赖 Response VO。
 *
 * <p>版本号、前后任务的任务编号与工序名称都由 Mapper JOIN 模块内表取出，列表不显示裸 ID。
 */
@Schema(description = "管理后台 - APS-排程任务依赖 Response VO")
@Data
@ExcelIgnoreUnannotated
public class TaskDepRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程版本编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long versionId;

    @Schema(description = "排程版本号")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "前置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long fromTaskId;

    @Schema(description = "前置任务编号")
    @ExcelProperty("前置任务编号")
    private String fromTaskNo;

    @Schema(description = "前置工序编码")
    @ExcelProperty("前置工序编码")
    private String fromOpCode;

    @Schema(description = "前置工序名称")
    @ExcelProperty("前置工序")
    private String fromOpName;

    @Schema(description = "后置排程任务编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long toTaskId;

    @Schema(description = "后置任务编号")
    @ExcelProperty("后置任务编号")
    private String toTaskNo;

    @Schema(description = "后置工序编码")
    @ExcelProperty("后置工序编码")
    private String toOpCode;

    @Schema(description = "后置工序名称")
    @ExcelProperty("后置工序")
    private String toOpName;

    @Schema(description = "依赖类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "FINISH_START")
    @ExcelProperty(value = "依赖类型", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.TASK_DEP_TYPE)
    private String depType;

    @Schema(description = "依赖间隔分钟数", requiredMode = Schema.RequiredMode.REQUIRED, example = "0")
    @ExcelProperty("依赖间隔分钟数")
    private Integer lagMinutes;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
