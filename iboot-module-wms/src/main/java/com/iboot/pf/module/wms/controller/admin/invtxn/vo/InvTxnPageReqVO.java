package com.iboot.pf.module.wms.controller.admin.invtxn.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - WMS-库存流水分页 Request VO")
@Data
public class InvTxnPageReqVO extends PageParam {

    @Schema(description = "库存事务编号")
    private String txnNo;

    @Schema(description = "事务类型", example = "RECEIPT")
    private String txnType;

    @Schema(description = "来源业务类型", example = "PO_IN")
    private String bizType;

    @Schema(description = "来源业务单号")
    private String bizNo;

    @Schema(description = "收发方向", example = "IN")
    private String ioType;

    @Schema(description = "仓库编号", example = "1")
    private Long whId;

    @Schema(description = "物料编号", example = "1")
    private Long matId;

    @Schema(description = "批次号快照")
    private String lotNo;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
