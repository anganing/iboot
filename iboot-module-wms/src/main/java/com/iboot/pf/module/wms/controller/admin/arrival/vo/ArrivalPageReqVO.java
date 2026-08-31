package com.iboot.pf.module.wms.controller.admin.arrival.vo;


import java.time.LocalDate;
import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-到货登记分页 Request VO")
@Data
public class ArrivalPageReqVO extends PageParam {

    @Schema(description = "到货登记单号")
    private String arrivalNo;

    @Schema(description = "来源类型", example = "PURCHASE_ORDER")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "供应商编号", example = "1")
    private Long supplierId;

    @Schema(description = "目标仓库编号", example = "1")
    private Long whId;

    @Schema(description = "状态", example = "DRAFT")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
