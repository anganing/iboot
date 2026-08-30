package com.iboot.pf.module.mes.controller.admin.qcplan.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-检验方案分页 Request VO")
@Data
public class QcPlanPageReqVO extends PageParam {

    @Schema(description = "检验方案编码")
    private String code;

    @Schema(description = "检验方案名称", example = "赵六")
    private String name;

    @Schema(description = "方案版本号")
    private String versionNo;

    @Schema(description = "抽样类型：FULL、FIXED_QTY、PERCENT、AQL", example = "2")
    private String sampleType;

    @Schema(description = "固定数量或抽样比例")
    private BigDecimal sampleValue;

    @Schema(description = "AQL接收质量限")
    private String aql;

    @Schema(description = "接收判定规则")
    private String acceptanceRule;

    @Schema(description = "状态：DRAFT、PUBLISHED、DISABLED", example = "1")
    private String status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}