package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;
import cn.idev.excel.annotation.*;
import com.iboot.pf.framework.excel.core.annotations.DictFormat;
import com.iboot.pf.framework.excel.core.convert.DictConvert;
import com.iboot.pf.module.wms.enums.WmsDictTypeConstants;

@Schema(description = "管理后台 - WMS-仓储策略 Response VO")
@Data
@ExcelIgnoreUnannotated
public class StrategyRespVO {

    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("主键")
    private Long id;

    @Schema(description = "策略编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("策略编码")
    private String code;

    @Schema(description = "策略名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("策略名称")
    private String name;

    @Schema(description = "策略类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "策略类型", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.STRATEGY_TYPE)
    private String strategyType;

    @Schema(description = "适用仓库编号，0表示全局", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @ExcelProperty("适用仓库编号，0表示全局")
    private Long whId;

    @Schema(description = "策略版本号", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("策略版本号")
    private String versionNo;

    /**
     * 库里的原始配置内容，只读。
     *
     * <p>不导出到 Excel：一格里塞一坨 JSON，看的人还得自己解析。页面上要用的是下面解析好的
     * {@link #config}，这个字段留着只为排查问题时能看到库里到底存了什么。
     */
    @Schema(description = "库里的原始配置内容，只读")
    private String configJson;

    @Schema(description = "解析后的策略配置")
    private StrategyConfigVO config;

    @Schema(description = "原始配置是否无法识别", example = "false")
    private Boolean configInvalid;

    @Schema(description = "原始配置无法识别的原因")
    private String configInvalidReason;

    @Schema(description = "优先级，数值越大越优先", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty(value = "状态", converter = DictConvert.class)
    @DictFormat(WmsDictTypeConstants.STRATEGY_STATUS)
    private String status;

    @Schema(description = "备注")
    @ExcelProperty("备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    @ExcelProperty("创建时间")
    private LocalDateTime createTime;


    @Schema(description = "适用仓库编号，0表示全局编码")
    @ExcelProperty("适用仓库编号，0表示全局编码")
    private String whCode;

    @Schema(description = "适用仓库编号，0表示全局名称")
    @ExcelProperty("适用仓库编号，0表示全局名称")
    private String whName;

}
