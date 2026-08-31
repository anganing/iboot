package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import cn.idev.excel.annotation.ExcelIgnoreUnannotated;
import cn.idev.excel.annotation.ExcelProperty;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.aps.enums.ApsDictTypeConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/** 排程策略 Response VO。 */
@Schema(description = "管理后台 - APS-排程策略 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StrategyRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "排程策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程策略编码")
    private String code;

    @Schema(description = "排程策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("排程策略名称")
    private String name;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED, example = "V1.0")
    @ExcelProperty("策略版本号")
    private String versionNo;

    @Schema(description = "优化目标说明", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优化目标说明")
    private String objective;

    @Schema(description = "硬约束、软约束及权重配置，JSON", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configJson;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "DRAFT")
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(ApsDictTypeConstants.STRATEGY_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;

}
