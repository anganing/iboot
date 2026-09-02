package com.iboot.pf.module.aps.controller.admin.version.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程版本 Response VO。
 *
 * <p>场景编码与名称由 Mapper JOIN 取出；发布人昵称由 Service 调 system 门面批量补全。
 */
@Schema(description = "管理后台 - APS-排程版本 Response VO")
@Data
@ExcelIgnoreUnannotated
public class VersionRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程场景编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long scenarioId;

    @Schema(description = "排程场景编码")
    @ExcelProperty("排程场景编码")
    private String scenarioCode;

    @Schema(description = "排程场景名称")
    @ExcelProperty("排程场景")
    private String scenarioName;

    @Schema(description = "排程版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("排程版本号")
    private Integer versionNo;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "SOLVING")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.VERSION_STATUS)
    private String status;

    @Schema(description = "硬约束得分，0 表示计划可行；小于 0 判为求解失败，不允许发布")
    @ExcelProperty("硬约束得分")
    private Long hardScore;

    @Schema(description = "软约束得分，各软约束加权罚分之和，只在同策略同场景的版本之间可比")
    @ExcelProperty("软约束得分")
    private Long softScore;

    @Schema(description = "求解开始时间")
    @ExcelProperty("求解开始时间")
    private LocalDateTime solveStartTime;

    @Schema(description = "求解结束时间")
    @ExcelProperty("求解结束时间")
    private LocalDateTime solveEndTime;

    @Schema(description = "发布时间")
    @ExcelProperty("发布时间")
    private LocalDateTime publishedTime;

    @Schema(description = "发布人员编号", example = "1")
    private Long publishedBy;

    @Schema(description = "发布人")
    @ExcelProperty("发布人")
    private String publishedByName;

    @Schema(description = "求解结果或失败信息")
    @ExcelProperty("求解信息")
    private String solverMessage;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
