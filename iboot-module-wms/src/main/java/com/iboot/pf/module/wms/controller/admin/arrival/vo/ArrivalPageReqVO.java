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

    @Schema(description = "来源类型：PURCHASE_ORDER、ASN、SALES_RETURN、OTHER", example = "1")
    private String sourceType;

    @Schema(description = "来源单号")
    private String sourceNo;

    @Schema(description = "供应商编号", example = "19938")
    private Long supplierId;

    @Schema(description = "供应商编码快照")
    private String supplierCode;

    @Schema(description = "供应商名称快照", example = "李四")
    private String supplierName;

    @Schema(description = "库存组织编号", example = "7601")
    private Long invOrgId;

    @Schema(description = "工厂编号", example = "20682")
    private Long plantId;

    @Schema(description = "目标仓库编号", example = "28154")
    private Long whId;

    @Schema(description = "预计到货日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expectDate;

    @Schema(description = "实际到货时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] arrivalTime;

    @Schema(description = "状态：DRAFT、ARRIVED、QC_PENDING、DONE、CANCELLED", example = "1")
    private String status;

    @Schema(description = "备注", example = "你猜")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}