package com.iboot.pf.module.mdm.controller.admin.bom.vo;

import java.time.LocalDate;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 物料清单分页 Request VO")
@Data
public class BomPageReqVO extends PageParam {

    @Schema(description = "物料编号", example = "3691")
    private Long matId;

    @Schema(description = "版本号")
    private String version;

    @Schema(description = "状态", example = "2")
    private Integer status;

    @Schema(description = "生效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] effectiveDate;

    @Schema(description = "失效日期")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDate[] expiryDate;

    @Schema(description = "备注", example = "随便")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
