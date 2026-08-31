package com.iboot.pf.module.wms.controller.admin.zone.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库区分页 Request VO")
@Data
public class ZonePageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库区编码")
    private String code;

    @Schema(description = "库区名称")
    private String name;

    @Schema(description = "库区类型", example = "RAW")
    private String zoneType;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
