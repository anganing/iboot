package com.iboot.pf.module.aps.controller.admin.scenario.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程场景 Response VO。
 *
 * <p>策略编码与名称由 Mapper JOIN {@code aps_strategy} 取出；工厂、车间名称由 Service 调用 mdm 门面
 * 批量补全，不在 Mapper 里跨库 JOIN。
 */
@Schema(description = "管理后台 - APS-排程场景 Response VO")
@Data
@ExcelIgnoreUnannotated
public class ScenarioRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程场景编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程场景编码")
    private String code;

    @Schema(description = "排程场景名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程场景名称")
    private String name;

    @Schema(description = "工厂编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long plantId;

    @Schema(description = "工厂名称")
    @ExcelProperty("工厂")
    private String plantName;

    @Schema(description = "车间编号，0 表示全工厂", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long workshopId;

    @Schema(description = "车间名称")
    @ExcelProperty("车间")
    private String workshopName;

    @Schema(description = "排程策略编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long strategyId;

    @Schema(description = "排程策略编码")
    @ExcelProperty("排程策略编码")
    private String strategyCode;

    @Schema(description = "排程策略名称")
    @ExcelProperty("排程策略")
    private String strategyName;

    @Schema(description = "计划窗口开始时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划窗口开始时间")
    private LocalDateTime planStartTime;

    @Schema(description = "计划窗口结束时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("计划窗口结束时间")
    private LocalDateTime planEndTime;

    @Schema(description = "输入数据快照时间")
    @ExcelProperty("输入数据快照时间")
    private LocalDateTime snapshotTime;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.SCENARIO_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
