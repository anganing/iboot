package com.iboot.pf.module.wms.controller.admin.strategy.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-仓储策略分页 Request VO")
@Data
public class StrategyPageReqVO extends PageParam {

    @Schema(description = "策略编码")
    private String code;

    @Schema(description = "策略名称", example = "芋艿")
    private String name;

    @Schema(description = "策略类型：PUTAWAY、PICK、REPLENISH", example = "1")
    private String strategyType;

    @Schema(description = "适用仓库编号，0表示全局", example = "4541")
    private Long whId;

    @Schema(description = "策略版本号")
    private String versionNo;

    @Schema(description = "策略条件与排序规则配置")
    private String configJson;

    @Schema(description = "优先级，数值越大越优先")
    private Integer priority;

    @Schema(description = "状态：DRAFT、ENABLED、DISABLED", example = "2")
    private String status;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}