package com.iboot.pf.module.aps.controller.admin.strategy.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - APS-排程策略分页 Request VO")
@Data
public class StrategyPageReqVO extends PageParam {

    @Schema(description = "排程策略编码")
    private String code;

    @Schema(description = "排程策略名称", example = "张三")
    private String name;

    @Schema(description = "策略版本号")
    private String versionNo;

    @Schema(description = "优化目标说明")
    private String objective;

    @Schema(description = "硬约束、软约束及权重配置")
    private String configJson;

    @Schema(description = "状态：DRAFT、ENABLED、DISABLED", example = "2")
    private String status;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}