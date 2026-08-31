package com.iboot.pf.module.wms.controller.admin.batchmgr.vo;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-批次主档分页 Request VO")
@Data
public class BatchPageReqVO extends PageParam {

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "批次号；无批次物料使用默认批次")
    private String lotNo;

    @Schema(description = "序列号；SN物料以数量为1的批次承载")
    private String snNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "供应商批次号")
    private String supplierLotNo;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}