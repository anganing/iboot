package com.iboot.pf.module.mdm.controller.admin.routeopmat.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 工序用料分页 Request VO")
@Data
public class RouteOpMatPageReqVO extends PageParam {

    @Schema(description = "路线工序编号", example = "30682")
    private Long routeOpId;

    @Schema(description = "物料编号", example = "11509")
    private Long matId;

    @Schema(description = "数量")
    private BigDecimal qty;

    @Schema(description = "计量单位编号", example = "25404")
    private Long uomId;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}