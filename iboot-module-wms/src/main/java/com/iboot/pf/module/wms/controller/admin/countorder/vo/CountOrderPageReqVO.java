package com.iboot.pf.module.wms.controller.admin.countorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-盘点单分页 Request VO")
@Data
public class CountOrderPageReqVO extends PageParam {

    @Schema(description = "盘点单号")
    private String countNo;

    @Schema(description = "盘点类型", example = "FULL")
    private String countType;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库区编号，0表示全部库区", example = "1")
    private Long zoneId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
