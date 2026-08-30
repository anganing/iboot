package com.iboot.pf.module.mes.controller.admin.prodreport.vo;

import lombok.*;
import java.util.*;
import io.swagger.v3.oas.annotations.media.Schema;
import com.iboot.pf.framework.common.pojo.PageParam;
import java.math.BigDecimal;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDateTime;

import static com.iboot.pf.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - MES-生产报工记录分页 Request VO")
@Data
public class ProdReportPageReqVO extends PageParam {

    @Schema(description = "报工单号")
    private String reportNo;

    @Schema(description = "生产工单编号", example = "29062")
    private Long prodOrderId;

    @Schema(description = "作业工单编号", example = "18129")
    private Long workOrderId;

    @Schema(description = "工序任务编号", example = "28825")
    private Long opTaskId;

    @Schema(description = "报工作业人员编号", example = "13784")
    private Long workerId;

    @Schema(description = "生产资源编号", example = "30495")
    private Long resId;

    @Schema(description = "班次编号", example = "1428")
    private Long shiftId;

    @Schema(description = "合格数量")
    private BigDecimal qtyGood;

    @Schema(description = "报废数量")
    private BigDecimal qtyScrap;

    @Schema(description = "返工数量")
    private BigDecimal qtyRework;

    @Schema(description = "本次作业开始时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] startTime;

    @Schema(description = "本次作业结束时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] endTime;

    @Schema(description = "报工时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] reportTime;

    @Schema(description = "状态：DRAFT、SUBMITTED、QC_PENDING、CONFIRMED、CANCELLED", example = "1")
    private String status;

    @Schema(description = "报工幂等键")
    private String idempotencyKey;

    @Schema(description = "备注", example = "你说的对")
    private String remark;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}