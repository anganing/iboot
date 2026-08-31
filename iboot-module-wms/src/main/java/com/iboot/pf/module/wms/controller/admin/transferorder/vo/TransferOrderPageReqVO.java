package com.iboot.pf.module.wms.controller.admin.transferorder.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存转移调拨单分页 Request VO")
@Data
public class TransferOrderPageReqVO extends PageParam {

    @Schema(description = "转移或调拨单号")
    private String transferNo;

    @Schema(description = "转移类型", example = "LOC_TRANSFER")
    private String transferType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源仓库编号", example = "1")
    private Long fromWhId;

    @Schema(description = "目标仓库编号", example = "1")
    private Long toWhId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
