package com.iboot.pf.module.wms.controller.admin.wave.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-出库波次分页 Request VO")
@Data
public class WavePageReqVO extends PageParam {

    @Schema(description = "波次编号")
    private String waveNo;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "波次类型", example = "BY_ORDER")
    private String waveType;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
