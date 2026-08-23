package com.iboot.pf.module.infra.controller.admin.coderule.vo;

import com.iboot.pf.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 编码规则分页 Request VO")
@Data
public class CodeRulePageReqVO extends PageParam {

    @Schema(description = "规则编码，模糊匹配", example = "purchase")
    private String ruleCode;
    @Schema(description = "规则名称，模糊匹配", example = "采购")
    private String name;
    @Schema(description = "状态", example = "0")
    private Integer status;
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
