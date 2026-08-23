package com.iboot.pf.module.mdm.controller.admin.uomconv.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 单位换算分页 Request VO")
@Data
public class UomConvPageReqVO extends PageParam {

    @Schema(description = "物料编号", example = "9455")
    private Long matId;

    @Schema(description = "来源计量单位编号", example = "7295")
    private Long fromUomId;

    @Schema(description = "目标计量单位编号", example = "14009")
    private Long toUomId;

    @Schema(description = "换算比例")
    private BigDecimal ratio;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}