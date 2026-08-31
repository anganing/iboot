package com.iboot.pf.module.wms.controller.admin.loc.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库位分页 Request VO")
@Data
public class LocPageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库区编号", example = "1")
    private Long zoneId;

    @Schema(description = "库位编码")
    private String code;

    @Schema(description = "库位类型", example = "ASRS_CELL")
    private String locType;

    @Schema(description = "巷道号")
    private String aisleNo;

    @Schema(description = "状态", example = "EMPTY")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
