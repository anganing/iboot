package com.iboot.pf.module.wms.controller.admin.invbalance.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-多维库存余额分页 Request VO")
@Data
public class InvBalancePageReqVO extends PageParam {

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库位编号", example = "1")
    private Long locId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "项目编号，0表示无项目归属", example = "1")
    private Long projectId;

    @Schema(description = "库存范围", example = "COMMON")
    private String stockScope;

    @Schema(description = "质量状态", example = "QUALIFIED")
    private String qualityStatus;

    @Schema(description = "物料编码快照")
    private String matCode;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
