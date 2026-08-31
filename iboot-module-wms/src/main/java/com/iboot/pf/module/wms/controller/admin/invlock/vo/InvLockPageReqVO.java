package com.iboot.pf.module.wms.controller.admin.invlock.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存冻结记录分页 Request VO")
@Data
public class InvLockPageReqVO extends PageParam {

    @Schema(description = "冻结编号")
    private String lockNo;

    @Schema(description = "来源业务类型", example = "PO_IN")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "库位编号", example = "1")
    private Long locId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
